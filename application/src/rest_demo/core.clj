(ns rest-demo.core
  (:require [org.httpkit.server :as server]
            [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer :all]
            [clojure.pprint :as pp]
            [clojure.string :as str]
            [clojure.data.json :as json]
            [clj-pdf.core :as pdf]
            [cadastro-de-pessoa.cpf :as cpf]
            [digest])
  (:gen-class))

;; ------ Helper functions --------

(defn crypto-cpf [cpf]
  (digest/sha-256 (str cpf)))

;; (crypto-cpf "45350813870")
;; => "4146848f92fcd7aa18837a4fe7c72e4566514eaac4c8757637cfef82719650da"

;; Collection Helper functions to add a new cpf

;; Verify and encrypt, if the cpf is valid.
(defn crypto-if-valid [cpf]
  (if (cpf/valid? cpf)
    (str (crypto-cpf cpf))))

;; Write result to pdf.
(defn pdf-out [encrypted-cpf]
  (pdf/pdf 
   [{}
    [:phrase (str encrypted-cpf)]]
   "doc.pdf"))

;; Composing the functions.
(defn pdf-crypted [cpf]
  (pdf-out (crypto-if-valid (str cpf))))

;; test (valid cpf)
;; (pdf-crypted "15069602861")

;; Get the parameter specified by cpf, from :params object, in request.
(defn getparameter [req cpf] (get (:params req) cpf))


;; --------- Response ------------
;; Return "doc.pdf" at the root directory.
(defn cpf-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body
   (->
    (pdf-crypted (str (:cpf (:params req)))))})
;; (cpf-handler "http://127.0.0.1:3000/cpf?cpf=45350813870")



;; ---------- Routes of the responses ---------
(defroutes app-routes
  (GET "/cpf" [] cpf-handler)  
  (route/not-found "Error, page not found!"))

; Our main entry function
(defn -main
  "This is our main entry point"
  [& args]
  (let [port (Integer/parseInt (or (System/getenv "PORT") "3000"))]
    ; Run the server with Ring.defaults middleware
    (server/run-server (wrap-defaults #'app-routes site-defaults) {:port port})
    ; Run the server without ring defaults
    ;(server/run-server #'app-routes {:port port})
    (println (str "Running webserver at http:/127.0.0.1:" port "/"))))
