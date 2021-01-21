(ns serinus.worker-test
  (:require
   [clojure.test :as t :refer [deftest is]]
   [serinus.worker :as sut]))

(deftest smoketest
  (is (= 1 1)))
