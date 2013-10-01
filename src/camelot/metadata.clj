(ns camelot.metadata
  (:import (org.apache.pdfbox.pdmodel PDDocument PDDocumentInformation)
           (java.io FileInputStream))
  (:require [clojure.string :as str]))

(defn get-metadata
  "Given the location of a PDF file, provides the metadata it holds"
  [filename]
  (let [doc  (PDDocument/load (FileInputStream. filename))
        info (.getDocumentInformation doc)]
    (try
      {:author   (.getAuthor info)
       :title    (.getTitle info)
       :subject  (.getSubject info)
       :keywords (if-let [keywords (.getKeywords info)]
                   (string/split keywords #", ")
                   [])
       :trapped  (.getTrapped info)
       :creator  (.getCreator info)
       :pages    (.getNumberOfPages doc)
       :created  (.getCreationDate info)
       :modified (.getModificationDate info)
       :producer (.getProducer info)}
      (finally (if (not (nil? doc))
                 (.close doc))))))

(defn set-metadata
  "Sets metadata to the provided PDDocument"
  [^PDDocument document data]
  (let [meta (PDDocumentInformation.)]
    (.setAuthor meta (data :author))
    (.setKeywords meta (str/join ", " (data :keywords)))
    (.setTitle meta (data :title))
    (.setDocumentInformation document meta)))
