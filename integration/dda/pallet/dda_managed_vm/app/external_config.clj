; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
(ns dda.pallet.dda-managed-vm.app.external-config
  (:require
    [schema.core :as s]
    [clojure.string :as str]
    [clojure.edn :as edn]
    [dda.config.commons.user-env :as user-env]
    [keypin.core :refer [defkey letval] :as k]))

(def ExternalConfigSchema
  {:provisioning {:ip s/Str
                  :login s/Str
                  :password s/Str}
   :user {:name s/Str
          :password s/Str
          (s/optional-key :email) s/Str
          (s/optional-key :ssh-pub-key) s/Str
          (s/optional-key :gpg-public-key) s/Str
          (s/optional-key :gpg-private-key) s/Str
          (s/optional-key :gpg-passphrase) s/Str}})

(defn provisioning-ip []
  (let [file user-config-path]
   (-> (ex-config file) :provisioning :ip)))

(defn provisioning-user []
  (let [file user-config-path]
   {:login (-> (ex-config file) :provisioning :login)
    :password (-> (ex-config file) :provisioning :password)}))

;TODO: works with keypin and additional recursive conversion protocols
;TODO: source: https://groups.google.com/forum/#!topic/clojure/1NzLnWUtj0Q
(defprotocol ConvertibleToClojure
  (->clj [o]))
(extend-protocol ConvertibleToClojure
  java.util.Map
  (->clj [o] (let [entries (.entrySet o)]
               (reduce (fn [m [^String k v]]
                         (assoc m (keyword k) (->clj v)))
                       {} entries)))

  java.util.List
  (->clj [o] (vec (map ->clj o)))

  java.lang.Object
  (->clj [o] o)

  nil
  (->clj [_] nil))

(defn as-clj-map
  [m]
  (->clj m))

(defn dispatch-file-type
  "Dispatches a string to a keyword which represents the file type."
  [file-name]
  (keyword (last (str/split file-name #"\."))))

(defmulti parse-config dispatch-file-type)
(defmethod parse-config :edn
  [file-path]
  (as-clj-map (k/read-config [file-path])))

(defn ex-config
  "reads external edn-config"
  [user-config]
  (parse-config user-config))

(def user-config-path
  "./user-config.edn")

(def ssh-pub-key
  (user-env/read-ssh-pub-key-to-config))

(defn user-config []
   (let [file user-config-path]
    {:myuser
      {:hashed-password  (-> (ex-config file) :user :password)
               :authorized-keys (-> (ex-config file) :user :ssh-pub-key)
               :gpg {:trusted-key {:public-key (-> (ex-config file) :user :gpg-public-key)
                                   :private-key (-> (ex-config file) :user :gpg-private-key)
                                   :passphrase (-> (ex-config file) :user :gpg-passphrase)}}}}))

(defn vm-config []
  (let [file user-config-path]
   {:vm-user :myuser
    :platform :virtualbox
    :user-email (-> (ex-config file) :user :email)}))
