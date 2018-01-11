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
    (let [date-from (util/today- 14)
          date-to (util/today)]
      (update-in db/default-db [:filters]
                 merge {:date-from date-from :date-to date-to}))))


(rf/reg-event-db
  ::check-one-checkbox
  (fn [db [_ filter-key value]]
    (rf/dispatch [::clear-idle-data])
    (update-in db [:filters filter-key] conj value)))


(rf/reg-event-db
  ::uncheck-one-checkbox
  (fn [db [_ filter-key value]]
    (rf/dispatch [::clear-idle-data])
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


(rf/reg-event-fx
  ::error-api-load
  (fn [_ [_ resp]]
    (let [err (get-in resp [:response :errors])]
      (js/console.log (str "load API error: " err)))))



(rf/reg-event-db
  ::clear-idle-by-geo
  (fn [db _]
    (assoc-in db [:idle :geo] [])))


(rf/reg-event-db
  ::clear-idle-by-group
  (fn [db _]
    (assoc-in db [:idle :group] [])))


(rf/reg-event-db
  ::clear-idle-by-geo-and-group
  (fn [db _]
    (assoc-in db [:idle :geo-and-group] [])))


(rf/reg-event-fx
  ::clear-idle-data
  (fn [_ _]
    (rf/dispatch [::clear-idle-by-geo])
    (rf/dispatch [::clear-idle-by-group])
    (rf/dispatch [::clear-idle-by-geo-and-group])))


(defn convert-idle-data [d]
  (let [t (:avg_time_in_minutes d)]
    (assoc d :avg_time_formatted (util/format-minutes t))))


(defn convert-idle-data-geo-and-group [d]
  (let [t (:avg_time_in_minutes d)
        geo (:zone_label d)
        group (:group_title d)]
    (assoc d :avg_time_formatted (util/format-minutes t) :order (str geo "-" group))))


(rf/reg-event-db
  ::set-status-idle-loading
  (fn [db [_ key]]
    (assoc-in db [:idle-loading key] true)))


(rf/reg-event-db
  ::unset-status-idle-loading
  (fn [db [_ key]]
    (assoc-in db [:idle-loading key] false)))

;(rf/dispatch [::unset-status-idle-loading :geo])

(rf/reg-event-db
  ::idle-by-geo-loaded
  (fn [db [_ resp]]
    (rf/dispatch [::unset-status-idle-loading :geo])
    (let [items (get-in resp [:result])]
      (assoc-in db [:idle :geo] (map convert-idle-data items)))))


(rf/reg-event-db
  ::idle-by-group-loaded
  (fn [db [_ resp]]
    (rf/dispatch [::unset-status-idle-loading :group])
    (let [items (get-in resp [:result])]
      (assoc-in db [:idle :group] (map convert-idle-data items)))))


(rf/reg-event-db
  ::idle-by-geo-and-group-loaded
  (fn [db [_ resp]]
    (rf/dispatch [::unset-status-idle-loading :geo-and-group])
    (let [items (get-in resp [:result])]
      (assoc-in db [:idle :geo-and-group] (map convert-idle-data-geo-and-group items)))))


(rf/reg-event-fx
  ::load-idle-by-geo
  (fn [_ _]
    {:http-xhrio {:method :post
                  :uri (util/uri "/q/idlebygeo")
                  :params @(rf/subscribe [::subs/filter-for-idle-request])
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::idle-by-geo-loaded]
                  :on-failure [::error-api-load]}}))


(rf/reg-event-fx
  ::load-idle-by-group
  (fn [_ _]
    {:http-xhrio {:method :post
                  :uri (util/uri "/q/idlebygroup")
                  :params @(rf/subscribe [::subs/filter-for-idle-request])
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::idle-by-group-loaded]
                  :on-failure [::error-api-load]}}))


(rf/reg-event-fx
  ::load-idle-by-geo-and-group
  (fn [_ _]
    {:http-xhrio {:method :post
                  :uri (util/uri "/q/idlebygeoandgroup")
                  :params @(rf/subscribe [::subs/filter-for-idle-request])
                  :timeout 10000
                  :format (json-request-format)
                  :response-format (json-response-format {:keywords? true})
                  :on-success [::idle-by-geo-and-group-loaded]
                  :on-failure [::error-api-load]}}))


(rf/reg-event-fx
  ::load-data-idle
  (fn [_ _]
    (rf/dispatch [::set-status-idle-loading :geo])
    (rf/dispatch [::load-idle-by-geo])
    (rf/dispatch [::set-status-idle-loading :group])
    (rf/dispatch [::load-idle-by-group])
    (rf/dispatch [::set-status-idle-loading :geo-and-group])
    (rf/dispatch [::load-idle-by-geo-and-group])))


(rf/reg-event-db
  ::clear-filter-geo-zones
  (fn [db _]
    (rf/dispatch [::clear-idle-data])
    (assoc-in db [:filters :geo-zones] #{})))


(rf/reg-event-db
  ::clear-filter-groups
  (fn [db _]
    (rf/dispatch [::clear-idle-data])
    (assoc-in db [:filters :groups] #{})))


(rf/reg-event-db
  ::invert-filter-geo-zones
  (fn [db _]
    (rf/dispatch [::clear-idle-data])
    (let [all-items (set @(rf/subscribe [::subs/geo-zones]))]
      (update-in db [:filters :geo-zones] #(clojure.set/difference all-items %)))))


(rf/reg-event-db
  ::invert-filter-groups
  (fn [db _]
    (rf/dispatch [::clear-idle-data])
    (let [all-items (set @(rf/subscribe [::subs/groups]))]
      (update-in db [:filters :groups] #(clojure.set/difference all-items %)))))


(rf/reg-event-db
  ::filter-dates-changed
  (fn [db [_ el-name el-value]]
    (rf/dispatch [::clear-idle-data])
    (assoc-in db [:filters (keyword el-name)] el-value)))
