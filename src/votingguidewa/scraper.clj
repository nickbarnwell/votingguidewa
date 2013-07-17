(ns votingguidewa.scraper
  (:require [org.httpkit.client :as http]
            [me.raynes.laser :as l]
            [votingguidewa.data-loaders :as data]
            [instaparse.core :as ip])) 

(def BASE-URL "https://weiapplets.sos.wa.gov/MyVote/OnlineVotersGuide")
(def ELECTION-TYPES [:Measures :Federal :Statewide :Legislative :Judical])

(def js-parse
  (ip/parser "js.grammar"))

(def js-ast-transform
  (partial ip/transform
    {:identifier str
     :string-literal str
     :param-list vector
     :function (fn [x y] {:name x :params y})}))

(defn extract-js-params [node]
           (let [params (-> node :attrs :onclick js-parse js-ast-transform)] params)) 

(defn election-url-params [id section]
  {:language"en"
   :electionId id 
   :countyCode "xx"
   :group (name section)})

(defn get-election-page [id section] 
  (let [res (cond
              (= section :Measures) 
              (http/get (str BASE-URL "/" (name section)) 
                        {:query-params (election-url-params id section)})
              :else 
              (http/get BASE-URL {:query-params (election-url-params id section)}))]
    (:body @res)))

(defn parse-results-page [page cb]
  (map extract-js-params (cb (l/parse page))))

(defn extract-candidates [page]
  (letfn [(parse-fn [d]
            (l/select d
                      (l/child-of 
                        (l/class= 
                          "BallotNameLink" 
                          "indentCandidate") 
                        (l/element= :a))))] 
    (parse-results-page page parse-fn)))

(defn extract-measures [page]
  (letfn [(parse-fn [d]
            (l/select d
                      (l/child-of
                        (l/class= "TreeLevel2")
                        (l/element= :a))))]
    (parse-results-page page parse-fn)))


(defn get-ballot-info [election-id]
  (map data/get-resource (flatten (into []
        (for [t ELECTION-TYPES]
          (let [page (get-election-page election-id t)] 
               (if (= t :Measures)
                 (extract-measures page)
                 (extract-candidates page))))))))
