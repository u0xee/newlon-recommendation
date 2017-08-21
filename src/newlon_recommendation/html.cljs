(ns newlon-recommendation.html
  (:require [clojure.walk :as walk]
            [clojure.string :as str]
            [newlon-recommendation.sentence.read :as r]))

(defn expand-tree-from [root children-of expanded-node?]
  (if (not (expanded-node? root))
    root
    (->> (children-of root)
         (mapv (let [en? (fn [node]
                           (and (not= node root)
                                (expanded-node? node)))]
                 (fn [child]
                   (expand-tree-from child children-of en?))))
         (#(if (empty? %) root %)))))

(defn id-tree->record-tree [id-tree by-id]
  (walk/postwalk-replace (into {} (for [[id sentence] by-id]
                                    [id {:id id :text sentence}]))
                         id-tree))

(defn walk-labeling-depth [root depth]
  (if (not (sequential? root))
    (assoc root :depth depth)
    (mapv #(walk-labeling-depth % (inc depth))
          root)))

(defn group-depth-paragraphs [root]
  (let [level-chunks (partition-by sequential? root)]
    (mapcat (fn [chunk]
              (if (not (sequential? (first chunk)))
                [chunk]
                (mapcat group-depth-paragraphs chunk)))
            level-chunks)))

(defn flatten-records->html [root supporting-ids expanded-ids]
  (let [root (if (sequential? root) root [root])
        paragraphs (partition-by :depth (flatten root))
        paragraphs (group-depth-paragraphs root)
        sentence->html (fn [{:keys [id text]}]
                         (let [expand-class (cond (contains? expanded-ids id) "expanded"
                                                  (contains? supporting-ids id) "expandable")
                               html-class (str "sentence" (when expand-class
                                                            (str " " expand-class)))]
                           (str "<a id=\"" id "\" class=\"" html-class "\">"
                                text "</a>")))
        paragraph-htmls
        (for [p paragraphs]
          (let [depth (:depth (first p))
                inner (apply str (interpose " " (map sentence->html p)))]
            (str "<p class=\"depth" depth "\">" inner "</p>")))]
    (apply str (interpose "\n" paragraph-htmls))))

(defn html-from [{:keys [by-id supporting-ids main-id]} expanded-ids]
  (-> (expand-tree-from main-id supporting-ids expanded-ids)
      (id-tree->record-tree by-id)
      (walk-labeling-depth 0)
      (flatten-records->html supporting-ids expanded-ids)))

(defn html-element-by-id [id]
  (. js/document getElementById id))

(defn set-inner-html [el html]
  (aset el "innerHTML" html))

(defn get-letter-text []
  (-> (html-element-by-id "letterText")
      (aget "innerHTML")
      (str/trim)))

(def letter-info (r/read-sentences (get-letter-text)))
