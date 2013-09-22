(defproject camelot "0.2.2-SNAPSHOT"
  :description "A fast, nimble PDF generation and manipulation library"
  :url "http://github.com/KushalP/camelot"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/data.xml "0.0.3"]
                 [org.apache.pdfbox/pdfbox "1.6.0"]
                 [org.apache.xmlgraphics/fop "1.0"]]
  :dev-dependencies [[lein-clojars "0.8.0"]])
