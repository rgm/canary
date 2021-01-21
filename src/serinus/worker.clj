(ns serinus.worker
  "Starts one worker go-loop to "
  (:require
   [clojure.edn        :as edn]
   [clojure.java.io    :as io]
   [clojure.core.async :as a]
   [clojure.spec.alpha :as s]
   [taoensso.timbre    :as timbre]))

;; * helpers {{{1

(defn read-canaries
  [path]
  (let [source (io/resource path)]
    (with-open [pbr (-> source io/reader clojure.lang.LineNumberingPushbackReader.)]
      (edn/read pbr))))

;; * example tests {{{1

;; A canary test is an arbitrary clojure fn arity that returns `[:success data]`
;; or `[:failure data]`

(defn ex-1 [] (timbre/info "running example 1"))

(defn ex-2 [] (timbre/info "running example 2"))

;; * {{{1

(defn run-canary
  [c]
  {:post [(s/tuple #{:success :failure} any?)]}
  [:success (:test c)])

;; * clip {{{1

(defn start!
  [notif-chan canary-resource-path]
  (timbre/info "starting worker")
  (let [canaries (read-canaries canary-resource-path)
        hopper (a/chan 128)]
    (a/go (loop [] (when-some [canary (a/<! hopper)]
                     (timbre/info "running canary" (:id canary))
                     (a/>! notif-chan (run-canary canary))
                     (recur)))
          (timbre/info "stopping worker"))
    (doseq [c canaries] (let [interval-ms (* 1000 (:interval c))]
                          (a/go-loop []
                           (a/<! (a/timeout interval-ms))
                           (when (a/>! hopper c) (recur)))))
    hopper))

(defn stop!
  [chan]
  (a/close! chan))
