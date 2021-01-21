(ns user
  (:require
   [serinus.main]
   [juxt.clip.repl :as clip.repl :refer [start stop reset]]
   [taoensso.timbre :as timbre]))

(timbre/set-level! :info)
(clip.repl/set-init! #(serinus.main/system-config :dev))

(comment
  (start)
  (stop)
  (reset))
