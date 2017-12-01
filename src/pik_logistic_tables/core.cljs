(ns pik-logistic-tables.core
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [pik-logistic-tables.config :as config]
              [pik-logistic-tables.views :as views]
              [pik-logistic-tables.events :as events]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [views/main-panel]
            (.getElementById js/document "app")))


(defn ^:export init []
  (rf/dispatch-sync [::events/init-db])
  (rf/dispatch-sync [::events/load-data])
  (dev-setup)
  (mount-root))
