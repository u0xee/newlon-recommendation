(ns newlon-recommendation.sentence.read
  (:require [clojure.string :as str]
            [newlon-recommendation.sentence :as sent]))

(defn- sentences-in [s]
  (mapv sent/canonicalized
        (re-seq sent/pattern s)))

(def empty-line #"\n\s*\n")
(defn read-sentences [s]
  (let [paragraph-strings (str/split s empty-line)
        paragraphs (mapv sentences-in paragraph-strings)
        by-id (into {} (for [p paragraphs
                             s p]
                         [(sent/id s) s]))
        supporting-ids
        (into {} (for [p paragraphs]
                   (let [ids-of-p (mapv sent/id p)]
                     [(first ids-of-p) ids-of-p])))]
    {:by-id          by-id
     :supporting-ids supporting-ids
     :main-id        (sent/id (ffirst paragraphs))}))
