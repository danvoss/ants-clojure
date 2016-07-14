(ns ants-clojure.core
  (:require [clojure.java.io :as io])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def ant-count 100)
(def ants (atom []))

(defn create-ants []
  (for [i (range 0 ant-count)]
    {:x (rand-int width)
     :y (rand-int height)}))

;; side effects (global var, saving to disk, etc) indicated with bang
(defn draw-ants! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    (.setFill context javafx.scene.paint.Color/BLACK)
    (.fillOval context (:x ant) (:y ant) 5 5)))

;; -> "threading": x-change and y-change do the same
(defn move-ant [ant]
  (Thread/sleep 1)
  (let [x-change (- (* 2 (rand)) 1)
        y-change (-> (rand) (* 2) (- 1))]
    (assoc ant 
      :x (+ (:x ant) x-change)
      :y (+ (:y ant) y-change))))

(defn move-ants []
  (pmap move-ant @ants)) ;; parallel map 

(def last-timestamp (atom 0))

(defn fps [current-timestamp]
  (let [diff (- current-timestamp @last-timestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))
  
;; dash indicates method override
;; dot after java class name indicates new class 
(defn -start [app stage]
  (let [root (javafx.fxml.FXMLLoader/load (io/resource "main.fxml"))
        scene (javafx.scene.Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [javafx.animation.AnimationTimer] []
                (handle [now]
                  (reset! ants (move-ants))
                  (draw-ants! context)
                  (.setText fps-label (str (fps now)))
                  (reset! last-timestamp now)))]
    (.setTitle stage "Ants")
    (.setScene stage scene)
    (.show stage)
    (reset! ants (create-ants))
    (.start timer)))  
      
(defn -main []
  (javafx.application.Application/launch ants_clojure.core (into-array String [])))
