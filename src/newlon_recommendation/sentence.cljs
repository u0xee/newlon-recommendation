(ns newlon-recommendation.sentence
  (:require [clojure.string :as str]))

(def example "She wasn't doing a thing that I could see, except standing there leaning on the balcony railing, holding the universe together.")

(defn subs-prefix [s prefix-length]
  (subs s 0 (min (count s) prefix-length)))

(defn pad-to-length [s new-length pad-char]
  (let [needed (- new-length (count s))
        pad (apply str (repeat needed pad-char))]
    (str s pad)))

(defn url-unsafe->underscore [s]
  (let [s (str/replace s #"[^\d\w$.+!*'(),-]+" "_")]
    s))

(defn id
  "Compute the ID of the given sentence.
  Expected to be unique."
  [sentence]
  (let [max-prefix 10
        prefix (url-unsafe->underscore (subs-prefix sentence max-prefix))
        padded-prefix (pad-to-length prefix (+ 2 max-prefix) \.)]
    (str padded-prefix (count sentence) \+ (bit-and 0xffff (hash sentence)))))

(def punctuation #{\. \! \?})
(def pattern #"[^.!?]+[.!?]+")

(defn canonicalized [sentence]
  (let [trimmed (str/trim sentence)
        last-char (nth trimmed (dec (count trimmed)))
        punctuated (str trimmed
                   (when (not (punctuation last-char))
                     \.))]
    (str/replace punctuated #"\s+" " ")))
