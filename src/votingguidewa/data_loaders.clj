(ns votingguidewa.data-loaders
  (:require [org.httpkit.client :as http]))

(def BASE-URL "https://weiapplets.sos.wa.gov/MyVote/OnlineVotersGuide/")
(def PRINTER-PARAM {"directLink" true})

(defn prep-args [args]
  (if (coll? args)
    args
    [args]))

(defn loader-fn [endpoint js-params ext-args]
  "Takes the JS function parameters as a vector of keyword arguments,
  the function parameters from the onclick as a vector, and any extra
  arguments passed to the function as a map of type <String, Object>
  
  Returns a function which calls the given endpoint with the params.
  Params should be a vector."

  (let [url (str BASE-URL endpoint)]
    (fn [js-args]
      (let [js-args (prep-args js-args)
            qps (merge (zipmap js-params js-args) ext-args)]
        {:src url
         :metadata qps
         :data (http/get url {:query-params qps})}))))



(defn get-measure [id]
  ((loader-fn  "MeasureDetail" ["measureId"] PRINTER-PARAM) id ))

(defn get-candidate [params]
  ((loader-fn "GetCandidateStatement"
        ["candidateId", "electionId", "termFullLabel",
              "raceJurisdictionName", "countyDisplay", "IsPartisanOffice",
              "RaceName", "language", "partyName", "group"] PRINTER-PARAM) params))


(defn get-resource [js-fn]
  (let [{fname :name params :params} js-fn]
    (if (.startsWith fname "fn")
      (apply get-measure params)
      (get-candidate params))))
