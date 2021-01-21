(ns serinus.notifier
  (:require
   [clojure.core.async :as a]
   [jsonista.core      :as json]
   [org.httpkit.client :as http]
   [taoensso.timbre    :as timbre]))

;; * notifiers {{{1

(defn slack-notifier
  [webhook-url msg]
  (let [payload {:text msg}]
    (http/request {:method  :post
                   :url     webhook-url
                   :headers {"Content-Type" "application/json"}
                   :body    (json/write-value-as-string payload)})))

;; * clip {{{1

(defn start!
  [{:keys [kind opts]}]
  (timbre/info "starting notifier")
  (let [chan     (a/chan 128)
        notifier (case kind
                   :slack #(slack-notifier (get opts :webhook-url) %)
                   #(timbre/info %))]
    (a/go (loop [] (when-some [[status data] (a/<! chan)]
                     (case status
                       :success (notifier data)
                       :failure (notifier data)
                       nil)
                     (recur)))
          (timbre/info "stopping notifier"))
    chan))

(defn stop!
  [chan]
  (a/close! chan))
