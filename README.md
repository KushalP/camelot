# Camelot

Camelot is a fast, nimble PDF generation and manipulation library.

[![Continuous Integration status](https://secure.travis-ci.org/KushalP/camelot.png)](http://travis-ci.org/KushalP/camelot)

### Why the name?

In the Spring of 1991, Dr. John Warnock released a paper which first described the [Camelot Project](http://www.planetpdf.com/planetpdf/pdfs/warnock_camelot.pdf). The project's goal was to solve a fundamental problem that confronts today's companies. The problem is concerned with our ability to communicate visual material between different computer applications and systems.

Camelot (this library) is named after the thoughts and discussions laid out in that paper.

## Usage

Add camelot as a dependency to your project. Jars are [published to clojars.org](https://clojars.org/camelot).

``` clojure
[camelot "0.1.3"]
```

Start using the library!

``` clojure
;; Create PDF files with some text.
(-> {:font "Helvetica-Bold"
     :size 12
     :text "Hello World!"
     :metadata {:author   "Joe Bloggs"
                :title    "Hello World"
                :keywords ["test" "hello" "world"]}}
    (save-as "/tmp/test.pdf"))

;; Merge a number of existing PDF files.
(-> ["filea.pdf" "fileb.pdf" "filec.pdf" "filed.pdf"]
    (merge-pdfs "/tmp/merged.pdf"))

;; Get a map of metadata from an existing PDF file.
(get-metadata "my-existing-document.pdf")
```

## Development

More than happy to accept patches! Once you are done with your changes and all tests pass, submit a pull request on Github.

## License

Copyright (C) 2012 Kushal Pisavadia

Distributed under the [Eclipse Public License](http://www.eclipse.org/legal/epl-v10.html), the same as Clojure.
