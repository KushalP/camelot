(ns camelot.test.fo
  (:use [clojure.test]
        [camelot.test.helpers]
        [camelot.fo] :reload)
  (:require [clojure.data.xml :as xml])
  (:import [java.io File]
           [org.apache.pdfbox.util PDFTextStripper]
           [org.apache.pdfbox.pdmodel PDDocument]))

(deftest test-layout
  (is (= (layout {})
         #clojure.data.xml.Element{
           :tag :fo:layout-master-set
           :attrs {}
           :content
             (#clojure.data.xml.Element{
               :tag :fo:simple-page-master
               :attrs {:master-name "first"
                       :margin-right "1.5cm"
                       :margin-left "1.5cm"
                       :margin-bottom "2cm"
                       :margin-top "1cm"
                       :page-width "211mm"
                       :page-height "297mm"}
               :content
                 (#clojure.data.xml.Element{
                   :tag :fo:region-body
                   :attrs {:margin-top "0cm"}
                   :content ()}
                 #clojure.data.xml.Element{
                   :tag :fo:region-before
                   :attrs {:extent "1cm"}
                   :content ()}
                 #clojure.data.xml.Element{
                   :tag :fo:region-after
                   :attrs {:extent "1.5cm"}
                   :content ()})})}))
  
  (is (= (layout {:margin-right "1:42cm"
                  :margin-left "2:42cm"
                  :margin-bottom "3:42cm"
                  :margin-top "4:42cm"
                  :page-width "5:42mm"
                  :page-height "6:42mm"
                  :region-body {:margin-top "3cm"}
                  :region-before {:extent "2cm"}
                  :region-after {:extent "1cm"}})
         #clojure.data.xml.Element{
           :tag :fo:layout-master-set
           :attrs {}
           :content
             (#clojure.data.xml.Element{
               :tag :fo:simple-page-master
               :attrs {:master-name "first"
                       :margin-right "1:42cm"
                       :margin-left "2:42cm"
                       :margin-bottom "3:42cm"
                       :margin-top "4:42cm"
                       :page-width "5:42mm"
                       :page-height "6:42mm"}
               :content
                 (#clojure.data.xml.Element{
                   :tag :fo:region-body
                   :attrs {:margin-top "3cm"}
                   :content ()}
                 #clojure.data.xml.Element{
                   :tag :fo:region-before
                   :attrs {:extent "2cm"}
                   :content ()}
                 #clojure.data.xml.Element{
                   :tag :fo:region-after
                   :attrs {:extent "1cm"}
                   :content ()})})})))

(deftest test-block?
  (are [v] (not (block? v))
       nil
       {:tag :fo:block}
       (xml/element :fo:not-block {} "hey"))

  (are [v] (block? v)
       (xml/element :fo:block {} "hey")
       (block "hey")))

(deftest test-block
  (is (= (block "Hic sunt dracones.")
         #clojure.data.xml.Element{
           :tag :fo:block
           :attrs {:text-align "left"
                   :font-size "10pt"
                   :line-height "14pt"}
           :content ("Hic sunt dracones.")}))

  (is (= (block {:space-before.optimum "10pt"
                 :space-after.optimum "20pt"
                 :text-align "end"}
                "Hic sunt dracones.")
         #clojure.data.xml.Element{
           :tag :fo:block
           :attrs {:text-align "end"
                   :font-size "10pt"
                   :line-height "14pt"
                   :space-before.optimum "10pt"
                   :space-after.optimum "20pt"}
           :content ("Hic sunt dracones.")}))

  (is (= (block [(block "A") (block "B")])
         #clojure.data.xml.Element{
           :tag :fo:block
           :attrs {:text-align "left"
                   :font-size "10pt"
                   :line-height "14pt"}
           :content (#clojure.data.xml.Element{
                       :tag :fo:block
                       :attrs {:text-align "left"
                               :font-size "10pt"
                               :line-height "14pt"}
                       :content ("A")}
                     #clojure.data.xml.Element{
                       :tag :fo:block
                       :attrs {:text-align "left"
                               :font-size "10pt"
                               :line-height "14pt"}
                       :content ("B")})})))

(deftest test-table-cell
  (is (= (table-cell "Foo")
         #clojure.data.xml.Element{
           :tag :fo:table-cell
           :attrs {}
           :content (#clojure.data.xml.Element{
                       :tag :fo:block
                       :attrs {:text-align "left"
                               :font-size "10pt"
                               :line-height "14pt"}
                       :content ("Foo")})}))
  (is (= (table-cell (block {:text-align "right"} "Foo"))
         #clojure.data.xml.Element{
           :tag :fo:table-cell
           :attrs {}
           :content (#clojure.data.xml.Element{
                       :tag :fo:block
                       :attrs {:text-align "right"
                               :font-size "10pt"
                               :line-height "14pt"}
                       :content ("Foo")})})))

(deftest test-table-row-has-attributes?
  (is (table-row-has-attributes? [{:foo "bar"} "A" "B" "C"]))
  (are [v] (not (table-row-has-attributes? v))
       [(block "A") "B" "C"]
       ["A" "B" "C"]))

(deftest test-table-row
  (is (= (table-row "A" "B" "C")
         #clojure.data.xml.Element{
           :tag :fo:table-row
           :attrs {}
           :content (#clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"}
                                   :content ("A")})}
                     #clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"}
                                   :content ("B")})}
                     #clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"}
                                   :content ("C")})})}))
  (is (= (table-row (block {:font-weight "bold"} "A") "B" "C")
         #clojure.data.xml.Element{
           :tag :fo:table-row
           :attrs {}
           :content (#clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"
                                           :font-weight "bold"}
                                   :content ("A")})}
                     #clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"}
                                   :content ("B")})}
                     #clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
                       :content (#clojure.data.xml.Element{
                                   :tag :fo:block
                                   :attrs {:text-align "left"
                                           :font-size "10pt"
                                           :line-height "14pt"}
                                   :content ("C")})})}))
  (is (= (table-row {:border-width "0.5pt" :keep-with-next "always"}
                    (block {:font-weight "bold"} "A"))
         #clojure.data.xml.Element{
           :tag :fo:table-row
           :attrs {:border-width "0.5pt"
                   :keep-with-next "always"}
           :content (#clojure.data.xml.Element{
                       :tag :fo:table-cell
                       :attrs {}
        :content (#clojure.data.xml.Element{
                    :tag :fo:block
                    :attrs {:text-align "left"
                            :font-size "10pt"
                            :line-height "14pt"
                            :font-weight "bold"}
                    :content ("A")})})})))

(deftest test-table-column
  (is (= (table-column "10cm")
         #clojure.data.xml.Element{:tag :fo:table-column
                                   :attrs {:column-width "10cm"}
                                   :content ()}))
  
    (is (= (table-column {:column-height "3cm"} "10cm")
         #clojure.data.xml.Element{:tag :fo:table-column
                                   :attrs {:column-width "10cm"
                                           :column-height "3cm"}
                                   :content ()})))

(deftest test-table
  (is (= (table ["5cm" "5cm"] [["a" "b"] ["c" "d"]])
         #clojure.data.xml.Element{
           :tag :fo:table
           :attrs {:border-width "0.5pt"}
           :content
             (#clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
                :tag :fo:table-body
                :attrs {}
                :content
                  (#clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {:border-width "0.5pt"
                    :keep-with-next "always"}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("a")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("b")})})}
                   #clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("c")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("d")})})})})}))
    (is (= (table {:foo "bar"} ["5cm" "5cm"] [["a" "b"] ["c" "d"]])
         #clojure.data.xml.Element{
           :tag :fo:table
           :attrs {:foo "bar"}
           :content
             (#clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
                :tag :fo:table-body
                :attrs {}
                :content
                  (#clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {:border-width "0.5pt"
                    :keep-with-next "always"}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("a")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("b")})})}
                   #clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("c")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("d")})})})})}))
  (is (= (table ["5cm" "5cm"] [[{:foo "bar"} "a" "b"] ["c" "d"]])
         #clojure.data.xml.Element{
           :tag :fo:table
           :attrs {:border-width "0.5pt"}
           :content
             (#clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
               :tag :fo:table-column
               :attrs {:column-width "5cm"}
               :content ()}
              #clojure.data.xml.Element{
                :tag :fo:table-body
                :attrs {}
                :content
                  (#clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {:foo "bar"}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("a")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("b")})})}
                   #clojure.data.xml.Element{
                    :tag :fo:table-row
                    :attrs {}
                    :content
                      (#clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("c")})}
                       #clojure.data.xml.Element{
                        :tag :fo:table-cell
                        :attrs {}
                        :content
                          (#clojure.data.xml.Element{
                            :tag :fo:block
                            :attrs {:text-align "left"
                                    :font-size "10pt"
                                    :line-height "14pt"}
                                    :content ("d")})})})})})))

(deftest test-region
  (is (= (region :static-content "xsl-region-before")
         (header)
         #clojure.data.xml.Element{:tag :fo:static-content
                                   :attrs {:flow-name "xsl-region-before"}
                                   :content ()}))
  (is (= (region :static-content "xsl-region-before" (block "A") (block "B"))
         (header (block "A") (block "B"))
         #clojure.data.xml.Element{:tag :fo:static-content
                                   :attrs {:flow-name "xsl-region-before"}
                                   :content
                                   (#clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                      :content ("A")}
                                    #clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                              :content ("B")})}))
  (is (= (region :static-content "xsl-region-after")
         (footer)
         #clojure.data.xml.Element{:tag :fo:static-content
                                   :attrs {:flow-name "xsl-region-after"}
                                   :content ()}))
  (is (= (region :static-content "xsl-region-after" (block "A") (block "B"))
         (footer (block "A") (block "B"))
         #clojure.data.xml.Element{:tag :fo:static-content
                                   :attrs {:flow-name "xsl-region-after"}
                                   :content
                                   (#clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                      :content ("A")}
                                    #clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                      :content ("B")})}))
  (is (= (region :flow "xsl-region-body")
         (body)
         #clojure.data.xml.Element{:tag :fo:flow
                                   :attrs {:flow-name "xsl-region-body"}
                                   :content ()}))
  (is (= (region :flow "xsl-region-body" (block "A") (block "B"))
         (body (block "A") (block "B"))
         #clojure.data.xml.Element{:tag :fo:flow
                                   :attrs {:flow-name "xsl-region-body"}
                                   :content
                                   (#clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                      :content ("A")}
                                    #clojure.data.xml.Element{
                                      :tag :fo:block
                                      :attrs {:text-align "left"
                                              :font-size "10pt"
                                              :line-height "14pt"}
                                              :content ("B")})})))

(deftest test-document
  (is (= (slurp "resources/fo/dummy-invoice.fo")
         (xml/indent-str
          (document {:header-blocks (map #(block {:text-align "end"} %)
                                         ["Telephone: +31 (0)6 48012240"
                                          "Street: IJskelderstraat 30"
                                          "Postal code: 5046 NK"
                                          "City: Tilburg"
                                          "Chamber of Commerce: 18068751"
                                          "VAT: 1903.14.849.B01"
                                          "Bank: Rabobank 3285.04.165"])}
                    (block {:font-size "35pt"
                            :space-before.optimum "0pt"
                            :space-after.optimum "15pt"}
                           "Vixu.com")
                    (block {:space-before.optimum "100pt"
                            :space-after.optimum "20pt"}
                           (map block ["BigCo"
                                       "Mr. John Doe"
                                       "Harteveltstraat 1"
                                       "2586 EL  Den Haag"
                                       "The Netherlands"]))
                    (block {:font-weight "bold"
                            :font-size "16"
                            :space-after.optimum "20pt"}
                           "Invoice")
                    (table {:border-width "0.5pt" :space-after.optimum "30pt"}
                           ["4cm" "5cm"]
                           [["Date:" "05/02/2012"]
                            ["Invoice number:" "BAZ01"]])
                    (table {:border-width "0.5pt"}
                           ["14cm" "3cm"]
                           [[(str "Vixu.com basic subscription from "
                                  "March 1st 2012 to March 1st 2013:")
                             (block {:text-align "right"} "€ 1188.00")]
                            ["Discount (10%):"
                             (block {:text-align "right"} "- € 118.80")]
                            ["Subtotal:"
                             (block {:text-align "right"} "€ 1070.00")]
                            ["Value Added Tax (19%):"
                             (block {:text-align "right"} "€ 203.30")]
                            [{:font-weight "bold"}
                             "Total:"
                             (block {:text-align "right"} "€ 1273.30")]])
                    (block {:space-before.optimum "35pt"}
                           (str "You are kindly requested to pay within 7 days. "
                                "Please wire the amount due to Rabobank account "
                                "number 3285.04.165.")))))))

(deftest test-write-pdf!
  (let [filename (temp-pdf-filename)]
    (do
      (write-pdf!
       (document {}
                 (block {:font-size "35pt"
                         :space-before.optimum "0pt"
                         :space-after.optimum "15pt"}
                        "Vixu.com"))
       filename))
    (is (= (.getText (PDFTextStripper.)
                     (PDDocument/load
                      (File. filename)))
           "Vixu.com\n"))))
