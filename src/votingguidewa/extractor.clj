(ns votingguidewa.extractor
  (:require [me.raynes.laser :as l]
            [clojure.zip :as z]
            [votingguidewa.util :refer :all])
  (:import (java.net URLDecoder)))

(def html-template
  (l/parse (slurp "candidate-template.html")))

(defn- decode-url [x] (URLDecoder/decode x "UTF-8"))

(defn- clean-val [x]
  ((comp decode-url clojure.string/trim) x))

(defn- generate-candidate-json [md html]
  {:id (Integer/parseInt (md "candidateId"))
   :type :candidate
   :listGroup (clean-val (md "RaceName"))
   :pivotGroup (md "group")
   :subheader (clean-val (md "partyName"))
   :header (-> (l/select (l/parse html) (l/class= "BallotName")) first :content first clean-val)
  })

(defn- request-html [c]
  (-> c :data deref :body))

(defn- candidate-html [c & [data]]
  (let [html (request-html c)
        parsed (l/parse html)
        header [(l/node :h1 :content (:header data)) (l/node :h2 :content (:subheader data))]
        content (-> (l/select parsed (l/element= :p)) first )
        data-table (-> (l/select parsed (l/element= :table)) last)]
    (l/document html-template
                (l/id= "header") (l/content header)
                (l/id= "main") (l/content content)
                (l/id= "footer") (l/content data-table))))

(defn candidate-static-data [c]
  (let [raw-html (request-html c)
        json (generate-candidate-json (:metadata c) raw-html)
        templated-html (candidate-html c json)]
    {:measure false :data json :html templated-html}))

;---- Measure Extraction ----;

(defn measure-static-data [m]
  (let [raw-html (request-html m)
        json {:id (do  1)}]
    {:measure true :data json :html raw-html}))
