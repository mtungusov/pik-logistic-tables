(ns pik-logistic-tables.views
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [clojure.string]
            [pik-logistic-tables.subs :as subs]
            [pik-logistic-tables.events :as events]))


(defonce ui-state (r/atom {:geo-zones-collapsed true
                           :groups-collapsed true
                           :dates-collapsed true}))


(defn chkbox [label value filter-key subs-checked?]
  (let [checked (rf/subscribe [subs-checked? value])]
    [:label {:key label
             :class "form-check-label"
             :style {:display :block}}
     [:input {:type "checkbox"
              :class "form-check-input"
              :value value
              :checked @checked
              :on-change (fn [e]
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


(defn date-input-from []
  (let [d @(rf/subscribe [::subs/filter-date-from])]
    [:input.input-sm.form-control {:type "text" :name "date-from"
                                   :default-value d}]))


(defn date-input-to []
  (let [d @(rf/subscribe [::subs/filter-date-to])]
    [:input.input-sm.form-control {:type "text" :name "date-to"
                                   :default-value d}]))


(defn dates-from-to-render []
  [:div#datepicker.input-daterange.input-group
   (date-input-from)
   [:span.input-group-addon "-"]
   (date-input-to)])


(defn dates-from-to-did-mount [this]
  (let [elem (js/$ (r/dom-node this))]
    (.datepicker elem (clj->js {:format "yyyy-mm-dd"
                                :endDate "0d"
                                :language "ru"
                                :autoclose true
                                :todayHighlight true}))
    (-> elem .datepicker (.on "changeDate" (fn [e]
                                             (let [n (.-target.name e)
                                                   d (.-target.value e)]
                                               (rf/dispatch [::events/filter-dates-changed n d])))))))


(defn dates-from-to []
  (r/create-class {:render dates-from-to-render
                   :component-did-mount dates-from-to-did-mount}))


(defn dates-block [state-key]
  (let [collapsed (state-key @ui-state)]
    [:div.dates-block
     (tool-header "Период" state-key "oi-calendar")
     [:div {:class (when collapsed "d-none")}
      [dates-from-to]]]))


(defn show-dates []
  (let [date-from @(rf/subscribe [::subs/filter-date-from])
        date-to @(rf/subscribe [::subs/filter-date-to])]
    [:div {:class (when (or (empty? date-from) (empty? date-to)) "d-none")}
     [:span.text-info "Период:"] [:span date-from]
     [:span.text-info "по"] [:span date-to]]))


(defn show-selected [label subs-name]
  (let [items (rf/subscribe [subs-name])]
    [:div {:class (when (empty? @items) "d-none")}
     [:span {:class "text-info"} label]
     (clojure.string/join ", " (sort @items))]))


(defn button-refresh []
  (let [date-from @(rf/subscribe [::subs/filter-date-from])
        date-to @(rf/subscribe [::subs/filter-date-to])
        geo-zones @(rf/subscribe [::subs/filter-geo-zones])
        groups @(rf/subscribe [::subs/filter-groups])
        hidden (or (empty? date-from)
                   (empty? date-to)
                   (empty? geo-zones)
                   (empty? groups))]
    [:button.btn.btn-sm.btn-success
     {:class (when hidden "d-none")
      :on-click (fn []
                  (println "Refresh data!"))}
     "Обновить"]))


(defn status-bar []
  [:div#status-bar
   (button-refresh)
   (show-dates)
   (show-selected "Геозоны:" ::subs/filter-geo-zones)
   (show-selected "Группы:" ::subs/filter-groups)])


(defn main-panel []
  [:div.row
   [:div.col-md-auto
    (dates-block :dates-collapsed)
    (geo-zones-block :geo-zones-collapsed)
    (groups-block :groups-collapsed)]
   [:div.col
    (status-bar)
    [:div "Table 1"]
    [:div "Table 2"]
    [:div "Table 3"]]])
