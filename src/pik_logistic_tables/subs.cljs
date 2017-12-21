(ns pik-logistic-tables.subs
  (:require [re-frame.core :as rf]))


(rf/reg-sub
  ::filter-geo-zones
  (fn [db _]
    (get-in db [:filters :geo-zones])))


(rf/reg-sub
  ::geo-zone-selected?
  (fn [_ _]
    (rf/subscribe [::filter-geo-zones]))
  (fn [items [_ value]]
    (contains? items value)))


(rf/reg-sub
  ::geo-zones
  (fn [db _]
    (sort (:geo-zones db))))


(rf/reg-sub
  ::filter-groups
  (fn [db _]
    (get-in db [:filters :groups])))


(rf/reg-sub
  ::group-selected?
  (fn [_ _]
    (rf/subscribe [::filter-groups]))
  (fn [items [_ value]]
    (contains? items value)))


(rf/reg-sub
  ::groups
  (fn [db _]
    (sort (:groups db))))


(rf/reg-sub
  ::filter-date-from
  (fn [db _]
    (get-in db [:filters :date-from])))


(rf/reg-sub
  ::filter-date-to
  (fn [db _]
    (get-in db [:filters :date-to])))


(rf/reg-sub
  ::filter-for-idle-request
  (fn [_ _]
    {:date_from @(rf/subscribe [::filter-date-from])
     :date_to   @(rf/subscribe [::filter-date-to])
     :zones     @(rf/subscribe [::filter-geo-zones])
     :groups    @(rf/subscribe [::filter-groups])}))


(rf/reg-sub
  ::idle-by-geo
  (fn [db _]
    (sort-by :zone_label (get-in db [:idle :geo]))))


(rf/reg-sub
  ::idle-by-group
  (fn [db _]
    (sort-by :group_title (get-in db [:idle :group]))))


(rf/reg-sub
  ::idle-by-geo-and-group
  (fn [db _]
    (sort-by :order (get-in db [:idle :geo-and-group]))))


(rf/reg-sub
  ::idle-loading-status
  (fn [db [_ key]]
    (get-in db [:idle-loading key])))

