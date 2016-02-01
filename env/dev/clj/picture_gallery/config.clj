(ns picture-gallery.config
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [picture-gallery.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[picture-gallery started successfully using the development profile]=-"))
   :middleware wrap-dev})
