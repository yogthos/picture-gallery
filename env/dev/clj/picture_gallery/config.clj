(ns picture-gallery.config
  (:require [selmer.parser :as parser]
            [taoensso.timbre :as timbre]
            [picture-gallery.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (timbre/info "\n-=[picture-gallery started successfully using the development profile]=-"))
   :middleware wrap-dev})
