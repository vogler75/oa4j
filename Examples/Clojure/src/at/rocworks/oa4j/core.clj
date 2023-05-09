(ns at.rocworks.oa4j.core
  (:import (at.rocworks.oa4j.base JClient JManager IHotLink)))

; clojure style functions for oa4j
(defn hl [f] (reify IHotLink (hotlink [this hotlink] (f hotlink))))

(defn dpConnect [dps callback]
  (let [f (JClient/dpConnect)]
    (doall (map #(.add f (name %)) dps))
    (.hotlink f (hl callback))
    (.connect f)))

(defn dpSet
  ([kv] (let [f (JClient/dpSet)]
            ;(doall (map #(.add x (name (first %)) (last %)) kv))
            (doall (map (fn [[k v]] (.add f (name k) v)) kv))
            (.send f)))
  ([k v] (JClient/dpSet (name k) v)))

(defn dpGet [dps]
  (if (or (set? dps) (sequential? dps))
    (let [res (JClient/dpGet (map #(name %) dps))]
      (map #(.getValueObject %) res))
    (let [var (JClient/dpGet (name dps))]
      (.getValueObject var))))

; callback function sums up all values and set the sum to a dp
(defn callback [xs]
  (let [v (reduce #(+ %1 %2) (map #(.getValueObject %) xs))];
    (dpSet :ExampleDP_Trend1. v)))

; manager instance
(def manager (new JManager))

; main
(defn -main [& args]
  ;(-> manager (.init (into-array args)) (.start))
  (.init manager (into-array args))
  (.start manager)

  ; dpGet
  (println "dpGet with str: " (dpGet :ExampleDP_Arg1.))
  (println "dpGet with set: " (dpGet #{:ExampleDP_Arg1. :ExampleDP_Arg2.}))
  (println "dpGet with seq: " (dpGet [:ExampleDP_Arg1. :ExampleDP_Arg2.]))

  ; dpGet and dpSet
  (let [[a1 a2] (dpGet [:ExampleDP_Arg1. :ExampleDP_Arg2.])]
    (dpSet {:ExampleDP_Arg1. (+ 1 a1) :ExampleDP_Arg2. (+ 1 a2)}))

  ; dpConnect
  (let [c (dpConnect [:ExampleDP_Arg1. :ExampleDP_Arg2.] callback)]
    (Thread/sleep 180000)
    (.disconnect c))

  (.stop manager))