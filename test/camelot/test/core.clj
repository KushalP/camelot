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

(deftest save-as-bad-input-throws-assertion-error
  (let [doc-map {:font "Helvetica-Bold"
                 :size 12
                 :text "Hello World"}
        filename (temp-pdf-filename)]
    (is (thrown? AssertionError (save-as [2 3] filename)))
    (is (thrown? AssertionError (save-as doc-map 23)))))

(deftest save-as-builds-a-basic-pdf-file
  (let [filename (temp-pdf-filename)
        doc      (-> {:font "Helvetica-Bold"
                      :size 12
                      :text "Hello World"}
                     (save-as filename))]
    (is (instance? PDDocument doc))
    (is (= "file" (file-kind filename)))
    (is (= 1 (.getPageCount doc)))))

(deftest save-as-builds-a-basic-pdf-file-with-metadata
  (let [filename (temp-pdf-filename)
        doc      (-> {:font "Helvetica-Bold"
                      :size 12
                      :text "Hello World"
                      :metadata {:author   "Joe Bloggs"
                                 :title    "Hello World"
                                 :keywords ["test" "hello" "world"]}}
                     (save-as filename))
        info     (.getDocumentInformation doc)]
    (is (instance? PDDocument doc))
    (is (= "file" (file-kind filename)))
    (is (= 1 (.getPageCount doc)))
    (is (= "Joe Bloggs" (.getAuthor info)))
    (is (= "test, hello, world" (.getKeywords info)))
    (is (= "Hello World" (.getTitle info)))))

(deftest merge-pdf-bad-input-throws-assertion-error
  (let [filename   (temp-pdf-filename)
        string-vec ["one" "two"]]
    (is (thrown? AssertionError (merge-pdfs [2 3] filename)))
    (is (thrown? AssertionError (merge-pdfs string-vec 23)))))

(deftest merge-pdf-joins-two-pdf-files
  (let [name-a (temp-pdf-filename)
        name-b (temp-pdf-filename)
        name-c (temp-pdf-filename)
        doc-a  (create-pdf-helper "File A" name-a)
        doc-b  (create-pdf-helper "File B" name-b)
        merged (-> [name-a name-b]
                   (merge-pdfs name-c))]
    ;; None of the filenames are the same.
    (is (not (= name-a name-b name-c)))
    ;; Make sure we have two PDF files that exist somewhere.
    (is (= "file" (file-kind name-a)))
    (is (= "file" (file-kind name-b)))
    (is (= 1 (.getPageCount doc-a)))
    (is (= 1 (.getPageCount doc-b)))
    (is (= 2 (.getPageCount merged)))))
