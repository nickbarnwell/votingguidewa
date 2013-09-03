(ns votingguidewa.core
  (:require [cheshire.core :as c]
            [votingguidewa.extractor :as e]
            [votingguidewa.scraper :as s]
            [votingguidewa.util :refer :all]))

(defn- out-path [id out]
  (str "data/" id (if (:measure out)
             "/measures/" "/candidates/") (:id (:data out)) ".html"))

(defn- out-data [record]
  (let [fn (if (:measure record)
    e/measure-static-data
    e/candidate-static-data)]
    (fn record)))

(defn create-static-data [id]
  (create-dir-tree ["data" (str "data/" id) (str "data/" id "/candidates") (str "data/" id "/measures")])
  (let [blob (into []
                   (doall
                     (for [itm (s/get-ballot-info id)]
                       (do
                         (spit (out-path id itm) (:html itm))
                         (:data itm)))))]
    (spit (str "data/" id "/data.json") (c/generate-string blob))))
