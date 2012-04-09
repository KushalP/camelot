(ns camelot.core
  (:require [clojure.string :as str])
  (:import (org.apache.pdfbox.pdmodel PDDocument PDDocumentInformation PDPage)
           (org.apache.pdfbox.pdmodel.edit PDPageContentStream)
           (org.apache.pdfbox.pdmodel.font PDType1Font)
           (org.apache.pdfbox.util PDFMergerUtility)
           (java.io FileInputStream))
  (:use [camelot.metadata]))

(defonce font-map
  {"Times-Roman"           PDType1Font/TIMES_ROMAN
   "Times-Bold"            PDType1Font/TIMES_BOLD
   "Times-Italic"          PDType1Font/TIMES_ITALIC
   "Times-BoldItalic"      PDType1Font/TIMES_BOLD_ITALIC
   "Helvetica"             PDType1Font/HELVETICA
   "Helvetica-Bold"        PDType1Font/HELVETICA_BOLD
   "Helvetica-Oblique"     PDType1Font/HELVETICA_OBLIQUE
   "Helvetica-BoldOblique" PDType1Font/HELVETICA_BOLD_OBLIQUE
   "Courier"               PDType1Font/COURIER
   "Courier-Bold"          PDType1Font/COURIER_BOLD
   "Courier-Oblique"       PDType1Font/COURIER_OBLIQUE
   "Courier-BoldOblique"   PDType1Font/COURIER_BOLD_OBLIQUE
   "Symbol"                PDType1Font/SYMBOL
   "ZapfDingbats"          PDType1Font/ZAPF_DINGBATS})

(defn font
  "Given a string representing a font, returns the equivalent ENUM."
  [^String name]
  (font-map name))

(defn save-as
  "Given a map representing the document to be built, and a filename, saves the PDF."
  [doc-map filename]
  {:pre [(and (map? doc-map)
              (string? filename))]}
  (let [page    (PDPage.)
        doc     (doto (PDDocument.)
                  (.addPage page))
        content (PDPageContentStream. doc page)]
    (try
      (.beginText content)
      (.setFont content (font (doc-map :font)) (doc-map :size))
      (.moveTextPositionByAmount content 100 700)
      (.drawString content (doc-map :text))
      (.endText content)
      (.close content)
      (when (contains? doc-map :metadata)
        (set-metadata doc (doc-map :metadata)))
      (.save doc filename)
      (finally (if (not (nil? doc))
                 (.close doc))))
    doc))

(defn merge-pdfs
  "Given a vector of existing PDF files (strings), and a filename to
   save them to, merge the PDF files into a single PDF and save it
   to the location filename"
  [pdf-files filename]
  {:pre [(and (string? filename)
              (every? string? pdf-files))]}
  (let [start-doc (PDDocument/load (FileInputStream. (first pdf-files)))
        pdfs      (rest pdf-files)]
    (try
      (loop [merger      (PDFMergerUtility.)
             destination start-doc
             files       pdfs]
        (if (empty? files)
          destination
          (do
            (let [next   (FileInputStream. (first pdfs))
                  source (PDDocument/load next)]
              (try
                (.appendDocument merger destination source)
                (finally
                 (.close source)
                 (.close next))))
            (recur merger
                   destination
                   (rest files)))))
      (.save start-doc filename)
      (finally (if (not (nil? start-doc))
                 (.close start-doc))))
    start-doc))
