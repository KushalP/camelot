(ns camelot.fo
  (:require [clojure.data.xml :as xml])
  (:import [java.io File BufferedOutputStream FileOutputStream StringReader]
           [org.apache.fop.apps FopFactory Fop MimeConstants]
           [javax.xml.transform Transformer TransformerFactory]
           [javax.xml.transform.stream StreamSource]
           [javax.xml.transform.sax SAXResult]))

(defn layout
  "Defines a :fo:layout-master-set using the given attribute map
   (i.e. :margin-right, :margin-left, :margin-bottom :margin-top,
   :page-width, :page-height, region-before, region-body, region-after;
    the last three being attribute maps for subnodes)."
  [{:keys [margin-right
           margin-left
           margin-bottom
           margin-top
           page-width
           page-height
           region-body
           region-before
           region-after]}]
  (xml/element :fo:layout-master-set
               {}
               (xml/element :fo:simple-page-master
                            {:master-name "first"
                             :margin-right (or margin-right "1.5cm")
                             :margin-left (or margin-left "1.5cm")
                             :margin-bottom (or margin-bottom "2cm")
                             :margin-top (or margin-top "1cm")
                             :page-width (or page-width "211mm")
                             :page-height (or page-height "297mm")}
                            (xml/element :fo:region-body
                                         (if (map? region-body)
                                           region-body
                                           {:margin-top "0cm"}))
                            (xml/element :fo:region-before
                                         (if (map? region-before)
                                           region-before
                                           {:extent "1cm"}))
                            (xml/element :fo:region-after
                                         (if (map? region-after)
                                           region-after
                                           {:extent "1.5cm"})))))

(defn block?
  "Returns true if the provided argument is a block"
  [x]
  (and (= (class x) clojure.data.xml.Element)
       (= (:tag x) :fo:block)))

(defn block
  "Defines a fo:block with the given attributes (optional) and
   content (which is a string or a sequence of blocks)..

   E.g. <fo:block font-size=\"10pt\"
                  line-height=\"14pt\"
                  text-align=\"end\">
            Hic sunt dracones
        </fo:block>"
  ([content]
     (block {} content))
  ([attrs content]
     (let [new-attrs (assoc attrs
                       :line-height (or (:line-height attrs) "14pt")
                       :font-size (or (:font-size attrs) "10pt")
                       :text-align (or (:text-align attrs) "left"))]
       (cond
        (string? content)
        (xml/element :fo:block new-attrs content)
        (every? block? content)
        (apply (partial xml/element :fo:block new-attrs) content)))))

(defn table-cell
  "Defines a fo:table-cell with either a provided string
   that returns a cell with default formatting or a (block)
   with custom formatting."
  [value]
  (xml/element :fo:table-cell
               {}
               (cond
                (string? value)
                (block value)
                (block? value)
                value)))

(defn table-row-has-attributes?
  "Returns true if the provided sequence starts with a map
   that isn't a block."
  [row]
  (and (map? (first row))
       (not (block? (first row)))))

(defn table-row
  "Defines a table row with an optional first argument
   containing an argument map, followed by the cell values."
  [& row]
  (let [has-attrs? (table-row-has-attributes? row)
        attrs (if has-attrs?
                (first row)
                {})
        values (if has-attrs?
                 (rest row)
                 row)]
    (apply (partial xml/element :fo:table-row attrs)
           (map table-cell values))))

(defn table-column
  "Defines a table column with the provided attributes (optional)
   and width."
  ([width]
     (table-column {} width))
  ([attrs width]
     (xml/element :fo:table-column
                  (assoc attrs
                    :column-width width)
                  nil)))

(defn table
  "Defines a table with rows generated from a provided
   sequence of vectors representing the individual rows.

   If there is no attribute map provided for the first row
   border-width=\"0.5pt\" and keep-with-next=\"always\"
   are used by default for this row"
  ([columns rows]
     (table {} columns rows))
  ([attrs columns rows]
     (apply (partial xml/element :fo:table
                     (if (not-empty attrs)
                       attrs
                       {:border-width "0.5pt"}))
            (conj
             (vec (map table-column columns))
             (apply
              (partial xml/element :fo:table-body {})
              (map-indexed (fn [i row]
                             (apply
                              table-row
                              (if (and (= i 0)
                                       (not (table-row-has-attributes? row)))
                                (cons {:border-width "0.5pt"
                                       :keep-with-next "always"}
                                      row)
                                row)))
                           rows))))))

(defn region [type name & blocks]
  "Defines a fo:static-content region with the given
   type (:static-content or :flow) and name that contains
   the given blocks.

   E.g. <fo:static-content flow-name=\"xsl-region-before\">
          <fo:block font-size=\"10pt\"
                    line-height=\"14pt\"
                    text-align=\"end\">Hic sunt dracones</fo:block>
        </fo:static-content>"
  (apply
   (partial xml/element
            (cond
             (= type :static-content) :fo:static-content
             (= type :flow) :fo:flow)
            {:flow-name name})
   blocks))

(def header
  (partial region :static-content "xsl-region-before"))

(def footer
  (partial region :static-content "xsl-region-after"))

(def body
  (partial region :flow "xsl-region-body"))

(defn document
  "Constructs a document with the :header-blocks and :footer-blocks
   from the provided settings map and all provided blocks."
  [settings & body-blocks]
  (xml/element :fo:root
               {:xmlns:fo "http://www.w3.org/1999/XSL/Format"}
               (layout (or (:layout settings) {}))
   (xml/element :fo:page-sequence {:master-reference "first"}
    (apply header (or (:header-blocks settings) [(block "")]))
    (apply footer (or (:footer-blocks settings) [(block "")]))
    (apply body body-blocks))))

(defn write-pdf!
  "Writes the provided document to-file as a PDF document."
  [document to-file]
  (with-open [out (BufferedOutputStream. (FileOutputStream. to-file))]
    (.transform (.newTransformer (TransformerFactory/newInstance))
                (StreamSource. (StringReader. (xml/emit-str document)))
                (SAXResult. (.getDefaultHandler
                             (.newFop (FopFactory/newInstance)
                                      MimeConstants/MIME_PDF out))))))
