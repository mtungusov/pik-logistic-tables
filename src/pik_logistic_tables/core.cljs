(ns pik-logistic-tables.core
    (:require [reagent.core :as r]
              [re-frame.core :as rf]
              [pik-logistic-tables.config :as config]
              [pik-logistic-tables.views :as views]))


(defn dev-setup []
  (when config/debug?
    (enable-console-print!)
    (println "dev mode")))


(defn mount-root []
  (rf/clear-subscription-cache!)
  (r/render [views/main-panel]
            (.getElementById js/document "app")))


(defn ^:export init []
  (dev-setup)
  (mount-root))
