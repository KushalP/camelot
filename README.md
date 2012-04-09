# Camelot

Camelot is a fast, nimble PDF generation and manipulation library.

[![Continuous Integration status](https://secure.travis-ci.org/KushalP/camelot.png)](http://travis-ci.org/KushalP/camelot)

## Usage

Add camelot as a dependency to your project. Jars are [published to clojars.org](https://clojars.org/camelot)

``` clojure
[camelot "0.1.1"]
```

Start using the library!

``` clojure
;; Create PDF files with some text.
(-> {:font "Helvetica-Bold"
     :size 12
     :text "Hello World"}
    (save-as "/tmp/test.pdf"))

;; Merge a number of existing PDF files.
(-> ["filea.pdf" "fileb.pdf" "filec.pdf" "filed.pdf"]
    (merge-pdfs "/tmp/merged.pdf"
```

## Development

More than happy to accept patches! Once you are done with your changes and all tests pass, submit a pull request on Github.

## License

Copyright (C) 2012 Kushal Pisavadia

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
