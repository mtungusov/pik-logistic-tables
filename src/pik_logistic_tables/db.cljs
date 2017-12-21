(ns pik-logistic-tables.db)


(def default-db
  {:filters {:geo-zones #{}
             :groups #{}
             :date-from "2017-01-01"
             :date-to "2017-01-02"}

   :geo-zones #{}

   :groups #{}

   :idle-loading {:geo false
                  :group false
                  :geo-and-group false}

   :idle {:geo []
          :group []
          :geo-and-group []}})
