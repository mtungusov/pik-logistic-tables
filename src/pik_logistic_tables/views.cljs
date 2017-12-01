(ns pik-logistic-tables.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [pik-logistic-tables.subs :as subs]
            [pik-logistic-tables.events :as events]))


(defonce ui-state (r/atom {:geo-zones-collapsed true
                           :groups-collapsed true}))


(defn chkbox [label value filter-key subs-checked?]
  (let [checked (rf/subscribe [subs-checked? value])]
    [:label {:key label
             :class "form-check-label"
             :style {:display :block}}
     [:input {:type "checkbox"
              :class "form-check-input"
              :value value
              :checked @checked
              :on-click (fn [e]
                          (.preventDefault e)
                          (if @checked
                            (rf/dispatch [::events/uncheck-one-checkbox filter-key value])
                            (rf/dispatch [::events/check-one-checkbox filter-key value])))}]
     label]))


(defn geo-zones []
  (let [items (rf/subscribe [::subs/geo-zones])]
    [:div.form-check.bg-light
     (doall
       (for [i @items]
         (chkbox i i :geo-zones ::subs/geo-zone-selected?)))]))


(defn groups []
  (let [items (rf/subscribe [::subs/groups])]
    [:div.form-check.bg-light
     (doall
       (for [i @items]
         (chkbox i i :groups ::subs/group-selected?)))]))


(defn tool-header [label state-key icon]
  (let [collapsed (state-key @ui-state)]
    [:div.tool-header.bg-light {:class "text-primary"
                                :on-click (fn []
                                            (swap! ui-state update state-key not))}
                                            ;(rf/dispatch [::events/selected->local-storage ui-key collapsed]))}
     [:span.oi {:class (str icon (when-not collapsed " d-none"))}]
     [:span {:class (when collapsed "d-none")} label]]))


(defn tool-buttons [ev-clear ev-invert]
  [:div.btn-toolbar {:role "toolbar"}
   [:div.btn-group {:role "group"}
    [:button.btn.btn-sm.btn-outline-danger {:type "button"
                                            :on-click (fn []
                                                        (rf/dispatch [ev-clear]))}
     [:span.oi.oi-ban]]
    [:button.btn.btn-sm.btn-outline-primary {:type "button"
                                             :on-click (fn []
                                                         (rf/dispatch [ev-invert]))}
     [:span.oi.oi-loop-circular]]]])


(defn geo-zones-block [state-key]
  (let [collapsed (state-key @ui-state)]
    [:div.chkbox-block
     (tool-header "Геозоны" state-key "oi-globe")
     [:div {:class (when collapsed "d-none")}
      (tool-buttons ::events/clear-filter-geo-zones ::events/invert-filter-geo-zones)
      (geo-zones)]]))


(defn groups-block [state-key]
  (let [collapsed (state-key @ui-state)]
    [:div.chkbox-block
     (tool-header "Группы транспорта" state-key "oi-grid-three-up")
     [:div {:class (when collapsed "d-none")}
      (tool-buttons ::events/clear-filter-groups ::events/invert-filter-groups)
      (groups)]]))


(defn main-panel []
  [:div.row
   [:div.col-md-auto
    (geo-zones-block :geo-zones-collapsed)
    (groups-block :groups-collapsed)]
   [:div.col
    [:div "Table 1"]
    [:div "Table 2"]
    [:div "Table 3"]]])
