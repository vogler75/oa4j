(ns at.rocworks.oa4j.core
  (:import (at.rocworks.oa4j.base JClient JManager IHotLink)))

; clojure style functions
(defn hl [f] (reify IHotLink (hotlink [this hotlink] (f hotlink))))

(defn dpConnect [dps callback]
  (let [x (JClient/dpConnect)]
    (doall (map #(.add x (name %)) dps))
    (.hotlink x (hl callback))
    (.connect x)))

(defn dpSet
  ([map] (let [x (JClient/dpSet)]
            (doall (map #(.add x (name (first %)) (last %)) map))
            (.send x)))
  ([dp value] (JClient/dpSet (name dp) value)))

(defn dpGet [dp]
  (if (sequential? dp)
    (let [res (JClient/dpGet (map #(name %) dp))]
      (map #(.getValueObject %) res))
    (let [var (JClient/dpGet (name dp))]
      (.getValueObject var))))

; test clojure style
(defn callback [dpIdValueList]
  (let [v (reduce #(+ %1 %2) (map #(.getValueObject %) (seq dpIdValueList)))];
    (dpSet :ExampleDP_Trend1. v)))

(def manager (new JManager))

(defn -main [& args]
  (.init manager (into-array args))
  (.start manager)
  (dpSet {:ExampleDP_Arg1. 2.0 :ExampleDP_Arg2. 3.0})
  (println (clojure.string/join "," (dpGet [:ExampleDP_Arg1. :ExampleDP_Arg2.])))
  (let [c (dpConnect [:ExampleDP_Arg1. :ExampleDP_Arg2.] callback)]
    (Thread/sleep 180000)
    (.disconnect c))
  (.stop manager))