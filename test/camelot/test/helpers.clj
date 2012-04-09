(ns camelot.test.helpers
  (:require [clojure.string :as str]))

(defn random-string
  "generates random string of 'length' characters"
  [length]
  (let [upper        "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        lower        (str/lower-case upper)
        numbers      "1234567890"
        alphanumeric (str upper lower numbers)]
    (loop [acc []]
      (if (= (count acc) length) (apply str acc)
          (recur (conj acc (rand-nth alphanumeric)))))))

(defn file-kind
  "Give a filename, returns the kind of File that resides at that location"
  [filename]
  (let [f (java.io.File. filename)]
    (cond
     (.isFile f)      "file"
     (.isDirectory f) "directory"
     (.exists f)      "other" 
     :else            "non-existent")))
