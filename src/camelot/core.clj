(ns camelot.core
  (:import (org.apache.pdfbox.pdmodel.font PDType1Font)))

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
