;START:ns
(ns picture-gallery.components.gallery
  (:require [picture-gallery.components.common :as c]
            [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [picture-gallery.ajax :as ajax]
            [clojure.string :as s]))
;END:ns

;START:set-background
(defn rgb-str [[r g b] mask]
  (str "rgba(" r "," g "," b "," mask ")"))

(defn set-background! [style [c1 c2 c3]]
  (set! (.-background style)
        (str "linear-gradient("
             (rgb-str c3 0.8) ","
             (rgb-str c2 0.9) ","
             (rgb-str c1 1) ")")))
;END:set-background

;START:image-panel
(defn image-panel-did-mount [thumb-link]
  (fn [div]
    (.getColors
      (js/AlbumColors. thumb-link)
      (fn [colors]
        (-> div reagent/dom-node .-style (set-background! colors))))))

(defn render-image-panel [link]
  (fn []
    [:img.image.panel.panel-default
     {:on-click #(session/remove! :modal)
      :src link}]))

(defn image-panel [thumb-link link]
  (reagent/create-class {:render      (render-image-panel link)
                         :component-did-mount (image-panel-did-mount thumb-link)}))
;END:image-panel

;START:image-modal
(defn image-modal [thumb-link link]
  (fn []
    [:div
     [image-panel thumb-link link]
     [:div.modal-backdrop.fade.in]]))
;END:image-modal

;START:delete-image
(defn delete-image! [name]
  (ajax/POST "/delete-image"
             {:params  {:image-name (s/replace name #"thumb_" "")
                        :thumb-name name}
              :handler #(do
                         (session/update-in!
                           [:thumbnail-links]
                           (fn [links]
                             (remove
                               (fn [link] (= name (:name link)))
                               links)))
                         (session/remove! :modal))}))
;END:delete-image

;START:delete-image-modal
(defn delete-image-button [owner name]
  (session/put!
    :modal
    (fn []
      [c/modal
       [:h2 "Remove " name "?"]
       [:div [:img {:src (str js/context "/gallery/" owner "/" name)}]]
       [:div
        [:button.btn.btn-primary
         {:on-click #(delete-image! name)}
         "delete"]
        [:button.btn.btn-danger
         {:on-click #(session/remove! :modal)}
         "Cancel"]]])))
;END:delete-image-modal

;START:thumb-link
(defn thumb-link [{:keys [owner name]}]
  [:td
   [:img
    {:src      (str js/context "/gallery/" owner "/" name)
     :on-click #(session/put!
                 :modal
                 (image-modal
                   (str js/context "/gallery/" owner "/" name)
                   (str js/context "/gallery/" owner "/" (s/replace name #"thumb_" ""))))}]
   (when (= (session/get :identity) owner)
     [:button.btn.btn-danger
      {:on-click #(delete-image-button owner name)}
      [:span.glyphicon.glyphicon-remove]])])
;END:thumb-link

;START:gallery
(defn gallery [links]
  [:table
   [:tbody
    (for [row (partition-all 3 links)]
      ^{:key row}
      [:tr
       (for [link row]
         ^{:key link}
         [thumb-link link])])]])
;END:gallery

;START:pager-helpers
(defn forward [i pages]
  (if (< i (dec pages)) (inc i) i))

(defn back [i]
  (if (pos? i) (dec i) i))

(defn nav-link [page i]
  [:li {:on-click #(reset! page i)
        :class    (when (= i @page) "active")}
   [:span i]])
;END:pager-helpers

;START:pager
(defn pager [pages page]
  (when (> pages 1)
    (into
      [:div.row.text-center>div.col-sm-12>ul.pagination.pagination-lg]
      (concat
        [[:li
          {:on-click #(swap! page back pages)
           :class    (when (= @page 0) "disabled")}
          [:span "«"]]]
        (map (partial nav-link page) (range pages))
        [[:li
          {:on-click #(swap! page forward pages)
           :class    (when (= @page (dec pages)) "disabled")}
          [:span "»"]]]))))
;END:pager

;START:fetch-gallery-thumbs
(defn fetch-gallery-thumbs! [owner]
  (ajax/GET (str "/list-thumbnails/" owner)
            {:handler #(session/put! :thumbnail-links %)}))
;END:fetch-gallery-thumbs

;START:partition-links
(defn partition-links [links]
  (when links
    (vec (partition-all 6 links))))
;END:partition-links

;START:gallery-page
(defn gallery-page []
  (let [page (atom 0)]
    (fn []
      [:div.container
       (when-let [thumbnail-links (partition-links (session/get :thumbnail-links))]
         [:div.row
          [:div.col-md-12
           [pager (count thumbnail-links) page]
           [gallery (thumbnail-links @page)]]])])))
;END:gallery-page
