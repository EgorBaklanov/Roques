(ns commands
  (:require [clojure.string :as str]
            [player :as player]
            [clojure.data.json :as json]
  )
)

(defn first-args [& args]
  (first args))

;коллизия игроков
(defn collision [player px py radius]
  (let [
      players (commute player/streams merge nil)
      f (atom 0)
    ]
    (doseq [[k v] players]
      (if (not= k "red:")
        (if (not= k "block:")
          (if (not= player k)
            (let [
                x (get v "x:")
                y (get v "y:")
              ]
              (if (< (+ (* (- px x) (- px x)) (* (- py y) (- py y))) (* (+ radius radius) (+ radius radius)))
                (
                  swap! f inc
                )
              )
            )
          )
        )
      )
    )
    (doseq [[k v] (@player/streams "red:")]
      (let [
        x (get v "x:")
        y (get v "y:")
        ]
        (if (< (+ (* (- px x) (- px x)) (* (- py y) (- py y))) (* (+ 5 5) (+ 5 5)))
          (
            (commute player/powers assoc player
            {:power true})
            (commute player/streams update-in ["red:"] dissoc k )
            (if (= true true)
              ;(
              (commute player/colorOchki assoc player {:ochki (+ (get (@player/colorOchki player ) :ochki) 1) } )
              ;увеличение скорости с каждым съеденным объектом
              ;(commute player/playermove assoc player {:move (+ (get (@player/playermove player ) :move) 1) } )
              ;)
            ) 
            
          )
        )
      )  
    )
    (doseq [[k v] (@player/streams "block:")]
      (let [
        x (get v "x:")
        y (get v "y:")
        ]
        (if (< (+ (* (- px x) (- px x)) (* (- py y) (- py y))) (* (+ radius radius) (+ radius radius)))
          (if (< px 40 )
            (commute player/streams assoc player {"x:" (+ px 100) "y:" py "ochki:" (get (@player/colorOchki player ) :ochki) })
            (if (> px 760)
              (commute player/streams assoc player {"x:" (- px 100) "y:" py "ochki:" (get (@player/colorOchki player ) :ochki) })
              (if (> py 560)
                (commute player/streams assoc player {"x:" px "y:" (- py 100) "ochki:" (get (@player/colorOchki player ) :ochki) })
                (commute player/streams assoc player {"x:" px "y:" (+ py 100) "ochki:" (get (@player/colorOchki player ) :ochki) })
              )
            )
          )
            ;(and (if (< px 100 ) ) (if (> py 500 ) ))
        )
        (if (< (+ (* (- px x) (- px x)) (* (- py y) (- py y))) (* (+ radius radius) (+ radius radius)))
          (
            swap! f inc
          )
        )
      )
    )
    (if (= @f 0)
      ;(commute player/streams assoc player {"x:" px "y:" py "ochki:" (+ (get (@player/streams player ) "ochki:") 1) })
      (commute player/streams assoc player {"x:" px "y:" py "ochki:" (get (@player/colorOchki player ) :ochki) })
    )
  )
)


;Command functions
(defn execute [input]
  (dosync
    (commute player/states assoc (str "player" player/*id* ":")
    (json/read-str input :key-fn keyword))
  )
)

;функция передвижения для каждого отдельного игрока
(defn moving  []
  (dosync
    (let [playerid (str "player" player/*id* ":")
          x (get (@player/streams playerid) "x:")
          y (get (@player/streams playerid) "y:")
          up (get (@player/states playerid) :up)
          down (get (@player/states playerid) :down)
          left (get (@player/states playerid) :left)
          right (get (@player/states playerid) :right)
        ]
      (if (= up true)
        (if (= left true) 
          (commute player/streams assoc playerid {"x:" (- x 5) "y:" (- y 5)}) 
          (if (= right true) 
            (commute player/streams assoc playerid {"x:" (+ x 5) "y:" (- y 5)})  
            (commute player/streams assoc playerid {"x:" x "y:" (- y 5)}) 
          )
        )
        (if (= down true) 
          (if (= left true) 
            (commute player/streams assoc playerid {"x:" (- x 5) "y:" (+ y 5)}) 
            (if (= right true) 
              (commute player/streams assoc playerid {"x:" (+ x 5) "y:" (+ y 5)})  
              (commute player/streams assoc playerid {"x:" x "y:" (+ y 5)}) 
            )
          ) 
          (if (= left true) 
            (commute player/streams assoc playerid {"x:" (- x 5) "y:" y}) 
            (if (= right true) 
              (commute player/streams assoc playerid {"x:" (+ x 5) "y:" y})  
              (commute player/streams assoc playerid {"x:" x "y:" y}) 
            )
          ) 
        )
      )
    )
  )
)

;функция передвижения сразу для всех игроков
(defn movingall  [radius]
  (dosync
    (let [playerid (str "player" player/*id* ":")
          players (commute player/streams merge nil)
          playerst (commute player/states merge nil)
        ]
      (doseq [[k v] players]
        (if (not= k "red:")
          (if (not= k "block:")
          (let [
            x (get v "x:")
            y (get v "y:")
            up  (get (get playerst k) :up)
            down  (get (get playerst k) :down)
            left  (get (get playerst k) :left)
            right (get (get playerst k) :right)
            move (get (@player/playermove k) :move)
            ]
            (if (= up true)
              (if (= left true) 
                ;(commute player/streams assoc k {"x:" (- x 5) "y:" (- y 5)}) 
                (collision k (- x move) (- y move) radius)
                (if (= right true) 
                  ;(commute player/streams assoc k {"x:" (+ x 5) "y:" (- y 5)})  
                  ;(commute player/streams assoc k {"x:" x "y:" (- y 5)}) 
                  (collision k (+ x move) (- y move) radius)
                  (collision k x (- y move) radius)
                )
              )
              (if (= down true) 
                (if (= left true) 
                  ;(commute player/streams assoc k {"x:" (- x 5) "y:" (+ y 5)}) 
                  (collision k (- x move) (+ y move) radius)
                  (if (= right true) 
                    ;(commute player/streams assoc k {"x:" (+ x 5) "y:" (+ y 5)})  
                    ;(commute player/streams assoc k {"x:" x "y:" (+ y 5)}) 
                    (collision k (+ x move) (+ y move) radius)
                    (collision k x (+ y move) radius)
                  )
                ) 
                (if (= left true) 
                  ;(commute player/streams assoc k {"x:" (- x 5) "y:" y}) 
                  (collision k (- x move) y radius)
                  (if (= right true) 
                    ;(commute player/streams assoc k {"x:" (+ x 5) "y:" y})  
                    ;(commute player/streams assoc k {"x:" x "y:" y}) 
                    (collision k (+ x move) y radius)
                    (collision k x y radius)
                  )
                ) 
              )
            )
          )
          )
        )
      )
    )
  )
)

;спавн красных точек

(defn spawnWalls [width height id]
  (dosync
    (let [
        x (rand-int width)
        y (rand-int height)
        blockt (@player/streams "block:")
      ]
      (commute player/streams assoc 
        "block:" 
        (assoc 
          blockt
          (str id ":")
          {"x:" x "y:" y}
        )
      )
    )
  )
)
(defn spawnred [width height id]
  (dosync
    (let [
        x (rand-int width)
        y (rand-int height)
        redt (@player/streams "red:")
      ]
      (commute player/streams assoc 
        "red:" 
        (assoc 
          redt 
          (str id ":")
          {"x:" x "y:" y}
        )
      )
    )
  )
)
;стенка
(defn stenka [width height id]
  (dosync
    (let [
        redt (@player/streams "block:")
      ]
      (commute player/streams assoc 
        "block:" 
        (assoc 
          redt 
          (str id ":")
          {"x:" width "y:" height}
        )
      )
    )
  )
)