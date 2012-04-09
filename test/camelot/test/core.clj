(ns camelot.test.core
  (:use [camelot.core]
        [camelot.test.helpers])
  (:use [clojure.test])
  (:import (org.apache.pdfbox.pdmodel PDDocument)
           (org.apache.pdfbox.pdmodel.font PDType1Font)))

(deftest converts-font-string-to-enum
  (is (= PDType1Font/TIMES_ROMAN (font "Times-Roman")))
  (is (= PDType1Font/TIMES_BOLD (font "Times-Bold")))
  (is (= PDType1Font/TIMES_ITALIC (font "Times-Italic")))
  (is (= PDType1Font/TIMES_BOLD_ITALIC (font "Times-BoldItalic")))
  (is (= PDType1Font/HELVETICA (font "Helvetica")))
  (is (= PDType1Font/HELVETICA_BOLD (font "Helvetica-Bold")))
  (is (= PDType1Font/HELVETICA_OBLIQUE (font "Helvetica-Oblique")))
  (is (= PDType1Font/HELVETICA_BOLD_OBLIQUE (font "Helvetica-BoldOblique")))
  (is (= PDType1Font/COURIER (font "Courier")))
  (is (= PDType1Font/COURIER_BOLD (font "Courier-Bold")))
  (is (= PDType1Font/COURIER_OBLIQUE (font "Courier-Oblique")))
  (is (= PDType1Font/COURIER_BOLD_OBLIQUE (font "Courier-BoldOblique")))
  (is (= PDType1Font/SYMBOL (font "Symbol")))
  (is (= PDType1Font/ZAPF_DINGBATS (font "ZapfDingbats"))))

(deftest save-as-builds-a-basic-pdf-file
  (let [filestr  (random-string 20)
        filename (str "/tmp/" filestr ".pdf")
        doc      (-> {:font "Helvetica-Bold"
                      :size 12
                      :text "Hello World"}
                     (save-as filename))]
    (is (instance? PDDocument doc))
    (is (= "file" (file-kind filename)))
    (is (= 1 (.getPageCount doc)))))
