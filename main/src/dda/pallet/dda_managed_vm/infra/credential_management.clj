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

(def UserHome s/Str)
(def RepoPath s/Str)
(def CredentialStore {:repo-path RepoPath})
(def CredentialStores [CredentialStore])

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
   ("ln" "-sf" "/usr/local/bin/gopass" "/usr/local/bin/pass")))

(s/defn configure-user-gopass-config
  [user-name :- s/Str
   user-home :- s/Str
   credential-stores :- CredentialStores]
   (let [std-passwordstore (selmer/render-file 
                            "gopass.yml.templ" (merge {:user-home user-home}
                                                      (first credential-stores)))
         passwordstorestomount (apply str (for [repo (rest credential-stores)] 
                                            (selmer/render-file
                                             "gopass_mount.yml.templ"
                                             (merge {:user-home user-home}
                                                    repo))))]
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
   credential-stores]
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
    (if (empty? credential-stores)
      (configure-user-gopass-config 
       user-name user-home [{:repo-path ".password-store"}])
      (configure-user-gopass-config 
       user-name user-home
       (map (fn [e] {:repo-path (str "repo/credential-store/" (:repo-name e))})
            credential-stores)))))

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
   credential-stores
   settings]
  (when (contains? settings :install-gopass)
    (configure-user-gopass facility user-name credential-stores)))
