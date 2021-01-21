(ns serinus.notifier
  (:require
   [clojure.core.async :as a]
   [taoensso.timbre    :as timbre]))

;; * clip {{{1

(defn start!
  []
  (timbre/info "starting notifier")
  (let [chan (a/chan 128)]
    (a/go (loop [] (when-some [[status log] (a/<! chan)]
                     (case status
                       :success (timbre/info log)
                       :failure (timbre/error log)
                       nil)
                     (recur)))
          (timbre/info "stopping notifier"))
    chan))

(defn stop!
  [chan]
  (a/close! chan))
