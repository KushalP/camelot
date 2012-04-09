# Camelot

Camelot is a fast, nimble PDF generation and manipulation library.

## Usage

Add camelot as a dependency to your project. Jars are [published to clojars.org](https://clojars.org/camelot)

    [camelot "0.1.0"]

Start using the library!

``` clojure
(-> {:font "Helvetica-Bold"
     :size 12
     :text "Hello World"}
    (save-as "/tmp/test.pdf"))
```

## Development

More than happy to accept patches! Once you are done with your changes and all tests pass, submit a pull request on Github.

## License

Copyright (C) 2012 Kushal Pisavadia

Distributed under the Eclipse Public License, the same as Clojure.
