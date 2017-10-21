(ns at.rocworks.winccoa
  (:import (at.rocworks.oa4j.base JClient JManager JDpConnect IHotLink)))

(defn hl [fu] (reify IHotLink (hotlink [this hotlink] (fu hotlink))))

(defn -main [& args]
  (let [manager (new JManager)
        connect (JClient/dpConnect)]
    (.init manager (into-array args))
    (.start manager)
    (doto connect
      (.add "ExampleDP_Arg1.")
      (.add "ExampleDP_Arg2.")
      (.hotlink (hl #(println %)))
      (.connect))
    (Thread/sleep 60000)
    (.disconnect connect)
    (.stop manager)))