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

; Simple Body Page
(defn simple-body-page [req] ;(3)
  {:status  200
   :headers {"Content-Type" "text/html"}
   :body    "Hello World"})
;
; request-example
(defn request-example [req]
     {:status  200
      :headers {"Content-Type" "text/html"}
      :body    (->
                (pp/pprint req)
                (str "Request Object: " req))})

; Hello-name handler
(defn hello-name [req]
     {:status  200
      :headers {"Content-Type" "text/html"}
      :body    (->
                (pp/pprint req)
                (str "Hello " (:name (:params req))))})

; my people-collection mutable collection vector
;; (def encrypted-cpf-collection (atom []))

;; Helper functions

(defn crypto-cpf [cpf]
  (digest/sha-256 (str cpf)))

;; (crypto-cpf "45350813870")
;; => "4146848f92fcd7aa18837a4fe7c72e4566514eaac4c8757637cfef82719650da"

;; Collection Helper functions to add a new cpf
;; (defn add-cpf [cpf]
;;   (swap! encrypted-cpf-collection conj {:encrypted-cpf (if (cpf/valid? cpf)
;;(crypto-cpf))}))

(defn crypto-if-valid [cpf]
  (if (cpf/valid? cpf)
    (str (crypto-cpf cpf))))

;; (crypto-if-valid "45350813870")
;; "4146848f92fcd7aa18837a4fe7c72e4566514eaac4c8757637cfef82719650da"

(defn pdf-out [encrypted-cpf]
  (pdf/pdf 
   [{}
    [:phrase (str encrypted-cpf)]]
   "doc.pdf"))

;; Composing the functions
(defn pdf-crypted [cpf]
   (-> cpf
       crypto-if-valid
       pdf-out))

;; (pdf-crypted "15069602861")


;;; Example JSON objects
;; (addperson "Functional" "Human")
;; (addperson "Micky" "Mouse")

;; Helper to get the parameter specified by cpf from :params object in req
(defn getparameter [req cpf] (get (:params req) cpf))

;; ;; Return List of People
(defn cpf-handler [req]
  {:status  200
   :headers {"Content-Type" "text/json"}
   :body     (-> (:cpf (:params req))
                 ;; (pp/pprint)
                 
                 (str "Hello "))})
;;                    (if (cpf/valid? cpf)
;;                      (crypto-cpf))
;;                    (json/write-str)
;;                    (pdf-out)))

;; (cpf/valid? "45350813870")

(cpf-handler "http://127.0.0.1:3000/cpf?cpf=45350813870")
;; {:status 200, :headers {"Content-Type" "text/json"}, :body "Hello "}




; Add a new person into the people-collection
;; (defn addperson-handler [req]
;;         {:status  200
;;          :headers {"Content-Type" "text/json"}
;;          :body    (-> (let [p (partial getparameter req)]
;;                         (str (json/write-str
;;                               (addperson (p :firstname) (p :surname))))))})

; Our main routes
(defroutes app-routes
  (GET "/" [] simple-body-page)
  (GET "/request" [] request-example)
  (GET "/hello" [] hello-name)
  ;; (GET "/people" [] people-handler)
  ;; (GET "/people/add" [] addperson-handler)
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
