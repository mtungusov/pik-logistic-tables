(ns pik-logistic-tables.util
  (:require [cljsjs.moment]
            [cljsjs.moment.locale.ru]
            [clojure.string :as string]
            [goog.string :as gstring]
            [pik-logistic-tables.config :as config]))


(.locale js/moment "ru")


(defn today []
  (.format (js/moment) config/db-date-format))


(defn today- [days]
  (.format (.subtract (js/moment) days "days") config/db-date-format))


;(today)
;(today- 10)


(defn uri [& path]
  (string/join "/" (concat [config/base-url config/api-version] path)))


(def one-hour 60)
(def one-day (* 24 one-hour))

(defn format-minutes [minutes]
  (when minutes
    (let [dd (/ minutes one-day)
          hh (/ (rem minutes one-day) one-hour)
          mm (rem (rem minutes one-day) one-hour)
          conv-f (comp #(gstring/padNumber % 2 0) js/Math.trunc)
          in-day (string/join ":" (map #(conv-f %) [hh mm]))]
      (if (>= dd 1)
        (str (gstring/padNumber dd 2 0) "д. " in-day)
        in-day))))

;(format-minutes 1571)
