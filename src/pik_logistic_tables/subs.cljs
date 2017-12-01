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
