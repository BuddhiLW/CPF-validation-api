(ns example
  (:use clj-pdf.core))

(pdf
  [{}
   [:list {:roman true}
          [:chunk {:style :bold} "a bold item"]
          "another item"
          "yet another item"]
   [:phrase "some text"]
   [:phrase "some more text"]
   [:paragraph "yet more text"]]
  "doc.pdf")
