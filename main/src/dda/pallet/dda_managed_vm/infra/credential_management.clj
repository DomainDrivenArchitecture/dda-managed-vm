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
(ns dda.pallet.dda-managed-vm.infra.credential-management
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]
    [selmer.parser :as selmer]
    [dda.config.commons.user-home :as user-env]))

(def Settings
  (hash-set :install-password-store :install-gopass))

(def CredentialStore s/Any)

(defn install-gpg
  []
  (actions/packages :aptitude ["rng-tools" "gnupg2"]))

(s/defn install-password-store
  [facility :- s/Keyword]
  (actions/as-action
   (logging/info (str facility "-install system: password-store")))
  (install-gpg)
  (actions/packages :aptitude ["pass"]))

(s/defn init-gopass
  [facility :- s/Keyword]
  (actions/as-action
   (logging/info (str facility "-init system: init-gopass")))
  (actions/package-source "gopass"
    :aptitude
    {:url "https://dl.bintray.com/gopasspw/gopass"
     :release "bionic"
     :scopes ["main"]
     :key-url "https://api.bintray.com/orgs/gopasspw/keys/gpg/public.key"}))

(s/defn install-gopass
  [facility :- s/Keyword]
  (actions/as-action
   (logging/info (str facility "-install system: init-gopass")))
  (actions/package-manager :update)
  (install-gpg)
  (actions/packages :aptitude ["gopass"])
  (actions/exec-checked-script
   "link gopass -> pass"
   ("ln" "-s" "/usr/local/bin/gopass" "/usr/local/bin/pass")))

(defn single-gopass-setup
  [user-name
   user-home
   repo-name]
  (actions/remote-file
   (str user-home "/.config/gopass/config.yml")
   :literal true
   :content (selmer/render-file 
             "gopass.yml.templ" 
             {:user-name user-name
              :path-to-repo (str user-home "/repo/credential-store/" repo-name)})
   :mode "644"
   :owner user-name
   :group user-name))

(defn multi-gopass-setup
  [user-name
   user-home
   credential-store]
   (let [std-passwordstore (selmer/render-file "gopass.yml.templ" {:user-home user-home
                                                                   :path-to-repo "/.password-store"})
         passwordstorestomount (apply str (for [repo credential-store] 
                                            (selmer/render-file
                                             "gopass_mount.yml.templ"
                                             {:repo-name (:repo-name repo)
                                              :user-home user-home})))]
     (actions/directory
      (str user-home "/.password-store")
      :owner user-name
      :group user-name)
     (actions/exec-checked-script
      "intialize .password-store"
      ("su" ~user-name "-c" "\"touch" ~(str user-home "/.password-store/.gpg-id\"")))
     (actions/remote-file
      (str user-home "/.config/gopass/config.yml")
      :literal true
      :content (str std-passwordstore passwordstorestomount)
      :mode "644"
      :owner user-name
      :group user-name)))

(s/defn configure-user-gopass
  [facility :- s/Keyword
   user-name
   credential-store]
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/as-action
     (logging/info (str facility "-configure user: configure-gopass")))
    (actions/exec-checked-script
     "config for gopass"
     ("su" ~user-name "-c" "\"mkdir" "-p" ~(str user-home "/.config/gopass/\""))
     ("su" ~user-name "-c" "\"touch" ~(str user-home "/.config/gopass/config.yml\"")))
    (actions/remote-file
     (str user-home "/.bashrc.d/gopass.sh")
     :literal true
     :content (selmer/render-file "gopass.sh.templ" {})
     :mode "644"
     :owner user-name
     :group user-name)
    (if (not (empty? credential-store))
      (if (= (count credential-store) 1)
        (single-gopass-setup user-name user-home (:repo-name (first credential-store)))
        (multi-gopass-setup user-name user-home credential-store)))))

(s/defn init-system
  [facility :- s/Keyword
   settings]
  (when (contains? settings :install-gopass)
    (init-gopass facility)))

(s/defn install-system
  [facility :- s/Keyword
   settings]
  (when (contains? settings :install-password-store)
    (install-password-store facility))
  (when (contains? settings :install-gopass)
    (install-gopass facility)))

(s/defn configure-user
  [facility :- s/Keyword
   user-name :- s/Str
   credential-store :- CredentialStore
   settings]
  (when (contains? settings :install-gopass)
    (configure-user-gopass facility user-name credential-store)))
