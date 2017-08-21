(ns newlon-recommendation.core
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require [newlon-recommendation.html :refer [html-from html-element-by-id
                                                set-inner-html letter-info]]
            [newlon-recommendation.url-state :refer [expanded-ids]]
            [clojure.core.async :as a]))

(defn route-click-to-ch [el ch]
  (aset el "onclick"
        (fn [mouse-event]
          (this-as element
            (a/put! ch
                    {:id (aget element "id")
                     :element element
                     :mouse-event mouse-event})))))

(def clicked-ch (a/chan 1))

(defn rebuild! []
  (set-inner-html (html-element-by-id "container")
                  (html-from letter-info @expanded-ids))
  (doseq [id (keys (:by-id letter-info))]
    (when-let [el (html-element-by-id id)]
      (route-click-to-ch el clicked-ch))))

(defn set-xor [s to-flip]
  (reduce (fn [s flip]
            (if (contains? s flip)
              (disj s flip)
              (conj s flip)))
          s to-flip))

(defn go-update-expanded-onClick! []
  (go-loop [{:keys [id element mouse-event]}
            (a/<! clicked-ch)]
    (when (contains? (:supporting-ids letter-info) id)
      (swap! expanded-ids set-xor [id])
      (rebuild!))
    (recur (a/<! clicked-ch))))

(rebuild!)
(go-update-expanded-onClick!)
