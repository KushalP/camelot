(ns camelot.test.metadata
  (:use [camelot.core]
        [camelot.metadata]
        [camelot.test.helpers])
  (:use [clojure.test])
  (:import (org.apache.pdfbox.pdmodel PDDocument)))

(deftest get-metadata-produces-map
  (let [filename (temp-pdf-filename)
        doc (-> {:font "Helvetica-Bold"
                 :size 12
                 :text "Hello World"
                 :metadata {:author   "Joe Bloggs"
                            :title    "Hello World"
                            :keywords ["test" "hello" "world"]}}
                (save-as filename))]
    (is (= (get-metadata filename)
           {:author   "Joe Bloggs"
            :title    "Hello World"
            :subject  nil
            :keywords ["test" "hello" "world"]
            :trapped  nil
            :creator  nil
            :pages    1
            :created  nil
            :modified nil
            :producer nil}))))

(deftest set-metadata-saves-metadata-to-pddocument
  (let [metadata {:author   "Joe Bloggs"
                  :title    "Hello World"
                  :keywords ["test" "hello" "world"]}
        doc      (PDDocument.)
        info     (.getDocumentInformation doc)]
    (is (not (= "Joe Bloggs" (.getAuthor info))))
    (is (not (= "Hello World" (.getTitle info))))
    (is (not (= "test, hello, world" (.getKeywords info))))
    (set-metadata doc metadata)
    (let [info (.getDocumentInformation doc)]
      (is (= "Joe Bloggs" (.getAuthor info)))
      (is (= "Hello World" (.getTitle info)))
      (is (= "test, hello, world" (.getKeywords info))))))
