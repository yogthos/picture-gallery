(ns picture-gallery.core
  (:require [reagent.core :as reagent :refer [atom]]
            [reagent.session :as session]
            [secretary.core :as secretary :include-macros true]
            [goog.events :as events]
            [goog.history.EventType :as EventType]
            [markdown.core :refer [md->html]]
            [picture-gallery.ajax :as ajax]
            [picture-gallery.components.registration :as r]
            [picture-gallery.components.login :as l]
            [picture-gallery.components.upload :as u]
            [picture-gallery.components.gallery :as g])
  (:import goog.History))

;START:account-actions
(defn account-actions [id]
  (let [expanded? (atom false)]
    (fn []
      [:li.dropdown
       {:class    (when @expanded? "open")
        :on-click #(swap! expanded? not)}
       [:a.dropdown-toggle
        [:span.glyphicon.glyphicon-user] " " id [:span.caret]
        [:ul.dropdown-menu
         [:li>a
          {:on-click
           #(session/put!
             :modal r/delete-account-modal)}
          "delete account"]
         [:li>a
          {:on-click
           #(ajax/POST
             "/logout"
             {:handler (fn [] (session/remove! :identity))})}
          "sign out"]]]])))
;END:account-actions

;START:user-menu
(defn user-menu []
  (if-let [id (session/get :identity)]
    [:li
     [:ul.nav.navbar-nav
      [:li
       [u/upload-button]]
      [account-actions id]]]
    [:li
     [:ul.nav.navbar-nav
      [:li [l/login-button]]
      [:li [r/registration-button]]]]))
;END:user-menu

(defn navbar []
  [:div.navbar.navbar-inverse.navbar-fixed-top
   [:div.container
    [:div.navbar-header>a.navbar-brand {:href "#/"} "myapp"]
    [:div.navbar-collapse.collapse
     [:ul.nav.navbar-nav
      [:li {:class (when (= :home (session/get :page)) "active")}
       [:a {:href "#/"} "Home"]]
      [:li {:class (when (= :about (session/get :page)) "active")}
       [:a {:href "#/about"} "About"]]]
     [:ul.nav.navbar-nav.navbar-right
      [user-menu]]]]])

(defn about-page []
  [:div "this is the story of picture-gallery... work in progress"])

;START:galleries
(defn galleries [gallery-links]
  [:div.text-center
   (for [row (partition-all 3 gallery-links)]
     ^{:key row}
     [:div.row
      (for [{:keys [owner name]} row]
        ^{:key (str owner name)}
        [:div.col-sm-4
         [:a {:href (str "#/gallery/" owner)}
          [:img {:src (str js/context "/gallery/" owner "/" name)}]]])])])

(defn list-galleries! []
  (ajax/GET "/list-galleries"
            {:handler #(session/put! :gallery-links %)}))

(defn home-page []
  (list-galleries!)
  (fn []
    [:div.container
     [:div.row
      [:div.col-md-12>h2 "Available Galleries"]]
     (when-let [gallery-links (session/get :gallery-links)]
       [:div.row>div.col-md-12
        [galleries gallery-links]])]))
;END:galleries

;START:pages
(def pages
  {:home    #'home-page
   :gallery #'g/gallery-page
   :about   #'about-page})
;END:pages

(defn modal []
  (when-let [session-modal (session/get :modal)]
    [session-modal]))

(defn page []
  [:div
   [modal]
   [(pages (session/get :page))]])

;; -------------------------
;; Routes
(secretary/set-config! :prefix "#")

;START:routes
(secretary/defroute "/" []
                    (session/put! :page :home))
(secretary/defroute "/gallery/:owner" [owner]
                    (g/fetch-gallery-thumbs! owner)
                    (session/put! :page :gallery))
(secretary/defroute "/about" []
                    (session/put! :page :about))
;END:routes

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      EventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (reagent/render [#'navbar] (.getElementById js/document "navbar"))
  (reagent/render [#'page] (.getElementById js/document "app")))

(defn init! []
  (hook-browser-navigation!)
  (session/put! :identity js/identity)
  (mount-components))
