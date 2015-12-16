(ns picture-gallery.ajax
  (:require [ajax.core :as ajax]
            [reagent.session :as session]))

(defn default-error-handler [response]
  (session/put! :error (get-in response [:response :message])))

(defn set-default-opts [opts]
  (-> opts
      (update
       :headers
       #(merge
         {"Accept" "application/transit+json"
          "x-csrf-token" js/csrfToken}
         %))
      (update :error-handler #(or % default-error-handler))))

(defn GET [url opts]
  (session/put! :user-event true)
  (ajax/GET (str js/context url) (set-default-opts opts)))

(defn POST [url opts]
  (session/put! :user-event true)
  (ajax/POST (str js/context url) (set-default-opts opts)))
