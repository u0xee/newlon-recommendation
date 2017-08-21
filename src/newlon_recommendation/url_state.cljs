(ns newlon-recommendation.url-state
  (:require [clojure.string :as str]
            [cljs.reader :refer [read-string]]))

(defn get-query-in-url []
  (-> (aget js/window "location" "href")
      (str/split #"[?]" 2)
      second))

(defn expanded-ids-in-url []
  (let [query (get-query-in-url)]
    (when (string? query)
      (let [query-decoded (-> query
                              (str/replace #"%22" "\"")
                              (str/replace #"%20" " "))
            key-val (->> (str/split query-decoded #"[&]")
                         (map (fn [k=v-string]
                                (let [[k v] (str/split k=v-string #"=")]
                                  [k (read-string v)])))
                         (into {}))
            exp (get key-val "expanded")]
        (when (coll? exp)
          exp)))))

(defn set-url-query! [new-query-string]
  (let [url-sans-query (-> (.. js/window -location -href)
                           (str/split #"[?]" 2)
                           first)]
    (.pushState (. js/window -history)
                {:docstring "State object passed to history"}
                "page title"
                (str url-sans-query "?" new-query-string))))

(defn install-in-url! [ids]
  (let [query (str "expanded=" (vec ids))]
    (set-url-query! query)))

(def expanded-ids (atom #{}))

(set-validator! expanded-ids
                (fn [new-ids]
                  (and (set? new-ids)
                       (every? string? new-ids))))

(add-watch expanded-ids :sync-to-url
           (fn [k r old new]
             (when (not= old new)
               (install-in-url! new))))

(defn sync-from-url! []
  (when-let [exp (expanded-ids-in-url)]
    (reset! expanded-ids (set exp))))

(sync-from-url!)
