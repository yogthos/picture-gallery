(ns picture-gallery.config
  (:require [taoensso.timbre :as timbre]))

(def defaults
  {:init
   (fn []
     (timbre/info "\n-=[picture-gallery started successfully]=-"))
   :middleware identity})
