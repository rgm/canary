(ns serinus.main
  "Main system entry point."
  (:require
   [aero.core]
   [clojure.java.io :as io]
   [juxt.clip.core :as clip]
   [taoensso.timbre :as timbre]))

(defn set-default-uncaught-exception-handler!
  "Sets the *global* default function to be called for uncaught exceptions on
   *any* thread. Individual threads can override this.
  (See https://stuartsierra.com/2015/05/27/clojure-uncaught-exceptions)."
  []
  (Thread/setDefaultUncaughtExceptionHandler
   (reify Thread$UncaughtExceptionHandler
     (uncaughtException [_ thread ex]
       (timbre/error ex "Uncaught exception on" (.getName thread))))))

(defonce system nil)

(defn system-config
  "Generate a system map for use by juxt.clip."
  [profile]
  (aero.core/read-config (io/resource "system.edn")
                         {:profile profile}))

(defn -main
  [& _]
  (set-default-uncaught-exception-handler!)
  (let [system-config (system-config :prd)
        system (clip/start system-config)]
    (alter-var-root #'system (constantly system))
    (.addShutdownHook
     (Runtime/getRuntime)
     (Thread. #(clip/stop system-config system))))
  @(promise))

(comment (system-config :dev))
