(ns votingguidewa.util)

 (defmacro def- [name & decls]
    (list* `def (with-meta name (assoc (meta name) :private true)) decls))

(def IMAGE-BASE-URL "https://weiapplets.sos.wa.gov" )
(def ELECTION-TYPES [:Measures :Federal :Statewide :Legislative :Judicial])

(defn create-dir-tree [dirs-vec]
  (dorun (map #(.mkdir (java.io.File. %)) dirs-vec)))
