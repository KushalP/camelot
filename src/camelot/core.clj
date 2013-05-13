(ns camelot.core
  (:import (org.apache.pdfbox.pdmodel PDDocument
                                      PDDocumentInformation
                                      PDPage)
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

(defonce page-sizes
  {:A0 PDPage/PAGE_SIZE_A0
   :A1 PDPage/PAGE_SIZE_A1
   :A2 PDPage/PAGE_SIZE_A2
   :A3 PDPage/PAGE_SIZE_A3
   :A4 PDPage/PAGE_SIZE_A4
   :A5 PDPage/PAGE_SIZE_A5
   :A6 PDPage/PAGE_SIZE_A6
   :letter PDPage/PAGE_SIZE_LETTER})

(defn font
  "Given a string representing a font, returns the equivalent ENUM."
  [^String name]
  (font-map name))

(defn draw-text-lines-for-page
  "Given PDocument, PDPage, PDRectangle and sequence of lines
   (e.g. [[{:font-size 10 :font-face \"Helvetica-Bold\"} \"Hello, World!\"}]),
   produces a PDocument with the provided text."
  [doc page page-size lines]
  (let [start-x 35
        start-y (- (.getUpperRightY page-size) 75)]
    (.addPage doc page)
    (with-open [content (PDPageContentStream. doc page)]
      (loop [lines lines
             start-y start-y]
        (when-let [[line-meta line-content] (first lines)]
          (let [font-face (font (or (:font-face line-meta) "Helvetica-Bold"))
                font-size (or (:font-size line-meta) 10)]
            (doto content
              (.setFont font-face font-size)
              (.beginText)
              (.moveTextPositionByAmount start-x start-y)
              (.drawString line-content)
              (.endText))
            (let [new-y (- start-y (* font-size 1.2))]
              (if (.contains page-size start-x (- new-y 30))
                (recur (rest lines) new-y)
                (do
                  (.close content)
                  (draw-text-lines-for-page doc
                                            (PDPage. page-size)
                                            page-size
                                            (rest lines))))))))))
  doc)

(defn save-as
  "Given a map representing the document to be built, and a filename,
  saves the PDF."
  [doc-map filename]
  {:pre [(and (map? doc-map)
              (string? filename))]}
  (with-open [doc (PDDocument.)]
    (let [page-size (get page-sizes (or (:page-size doc-map) :A4))]
      (draw-text-lines-for-page doc
                                (PDPage. page-size)
                                page-size
                                (:lines doc-map)))
    (when (contains? doc-map :metadata)
      (set-metadata doc (:metadata doc-map)))
    (.save doc filename)
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
            (let [next   (FileInputStream. (first files))
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
