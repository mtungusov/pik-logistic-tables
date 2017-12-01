(ns pik-logistic-tables.util
  (:require [cljsjs.moment]
            [cljsjs.moment.locale.ru]
            [clojure.string :as string]
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

