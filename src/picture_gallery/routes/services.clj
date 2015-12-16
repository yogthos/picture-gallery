(ns picture-gallery.routes.services
  (:require [picture-gallery.routes.services.auth :as auth]
            [picture-gallery.routes.services.upload :as upload]
            [picture-gallery.routes.services.gallery :as gallery]
            [ring.util.http-response :refer :all]
            [compojure.api.sweet :refer :all]
            [compojure.api.upload :refer :all]
            [schema.core :as s]))

(s/defschema UserRegistration
  {:id                     String
   :pass                   String
   :pass-confirm           String})

(s/defschema Result
  {:result                   s/Keyword
   (s/optional-key :message) String})

;START:galleries
(s/defschema Gallery
  {:owner               String
   :name                String
   (s/optional-key :rk) s/Num})
;END:galleries

(defapi service-routes
        (ring.swagger.ui/swagger-ui
          "/swagger-ui"
          :swagger-docs "/public.json")
        (swagger-docs
          "/public.json"
          {:info {:title "Public api"}})

        (POST* "/register" req
               :body [user UserRegistration]
               :summary "register a new user"
               :return Result
               (auth/register! req user))
        (POST* "/login" req
               :header-params [authorization :- String]
               :summary "login the user and create a session"
               :return Result
               (auth/login! req authorization))
        (POST* "/logout" []
               :summary "remove user session"
               :return Result
               (auth/logout!))
        ;START:get-image-list-thumbs
        (GET* "/gallery/:owner/:name" []
              :summary "display user image"
              :path-params [name :- String]
              (gallery/get-image name))
        (GET* "/list-thumbnails/:owner" []
              :path-params [owner :- String]
              :summary "list thumbnails for images in the gallery"
              :return [Gallery]
              (gallery/list-thumbnails owner))
        ;END:get-image-list-thumbs
        ;START:list-galleries
        (GET* "/list-galleries" []
              :summary "lists a thumbnail for each user"
              :return [Gallery]
              (gallery/list-galleries))
        ;END:list-galleries
        )

(defapi restricted-service-routes
        (swagger-ui "/swagger-ui-restricted"
                    :swagger-docs "/restricted.json")
        (swagger-docs "/restricted.json"
                      {:info {:title "Private api"}})
        (POST* "/upload" req
               :multipart-params [file :- TempFileUpload]
               :middlewares [wrap-multipart-params]
               (upload/save-image! (:identity req) file))

        (POST* "/delete-image" req
               :body-params [image-name :- String thumb-name :- String]
               :summary "delete the specified file from the database"
               :return Result
               (gallery/delete-image! (:identity req) thumb-name image-name))
        ;START:delete-account
        (POST* "/delete-account" req
               (auth/delete-account! (:identity req)))
        ;END:delete-account
        )
