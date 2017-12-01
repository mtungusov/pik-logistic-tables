(ns pik-logistic-tables.events
  (:require [re-frame.core :as rf]
            [day8.re-frame.http-fx]
            [ajax.core :refer [json-request-format json-response-format]]
            [clojure.set]
            [pik-logistic-tables.db :as db]
            [pik-logistic-tables.util :as util]
            [pik-logistic-tables.subs :as subs]))


(rf/reg-event-db
  ::init-db
  (fn [_ _]
    (let [date-from (util/today- 1)
          date-to (util/today)]
      (update-in db/default-db [:filters]
                 merge {:date-from date-from :date-to date-to}))))


(rf/reg-event-db
  ::check-one-checkbox
  (fn [db [_ filter-key value]]
    (update-in db [:filters filter-key] conj value)))


(rf/reg-event-db
  ::uncheck-one-checkbox
  (fn [db [_ filter-key value]]
    (update-in db [:filters filter-key] disj value)))


(rf/reg-event-fx
  ::error-api
  (fn [_ _]
    (js/console.log "API error!")))


(rf/reg-event-db
  ::geo-zones-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :geo-zones (set items)))))


(rf/reg-event-db
  ::groups-loaded
  (fn [db [_ resp]]
    (let [items (get-in resp [:result])]
      (assoc db :groups (set items)))))


(rf/reg-event-fx
  ::load-geo-zones
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (util/uri "q/zones")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::geo-zones-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-groups
  (fn [_ _]
    {:http-xhrio {:method :get
                  :uri (util/uri "q/groups")
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::groups-loaded]
                  :on-failure [::error-api]}}))


(rf/reg-event-fx
  ::load-data
  (fn [_ _]
    (rf/dispatch [::load-geo-zones])
    (rf/dispatch [::load-groups])))


(rf/reg-event-db
  ::clear-filter-geo-zones
  (fn [db _]
    (assoc-in db [:filters :geo-zones] #{})))


(rf/reg-event-db
  ::clear-filter-groups
  (fn [db _]
    (assoc-in db [:filters :groups] #{})))


(rf/reg-event-db
  ::invert-filter-geo-zones
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/geo-zones]))]
      (update-in db [:filters :geo-zones] #(clojure.set/difference all-items %)))))


(rf/reg-event-db
  ::invert-filter-groups
  (fn [db _]
    (let [all-items (set @(rf/subscribe [::subs/groups]))]
      (update-in db [:filters :groups] #(clojure.set/difference all-items %)))))

