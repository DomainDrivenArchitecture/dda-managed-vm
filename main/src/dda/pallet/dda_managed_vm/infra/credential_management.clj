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

(defn install-gpg
  []
  (actions/packages :aptitude ["rng-tools" "gnupg2"]))

(s/defn install-password-store
  [facility :- s/Keyword]
  (actions/as-action
   (logging/info (str facility "-install system: password-store")))
  (install-gpg)
  (actions/packages :aptitude ["pass"]))

(s/defn configure-password-store
  [facility :- s/Keyword
   user-name]
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/as-action
     (logging/info (str facility "-configure user: configure-password-store")))
    (actions/remote-file
     (str user-home "/.demo-pass")
     :owner user-name
     :group user-name
     :link (str user-home "/repo/credential-store/password-store-for-teams"))
    (actions/remote-file
     (str user-home "/.bashrc.d/team-pass.sh")
     :literal true
     :content "# Load the custom .*-pass I have
for i in ~/.*-pass; do
  [ -e $i/.load.bash ] && . $i/.load.bash
done
"
     :mode "644"
     :owner user-name
     :group user-name)))

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
  (actions/packages :aptitude ["gopass"]))
  ;au BufNewFile,BufRead /dev/shm/gopass.* setlocal noswapfile nobackup noundofile
  ;ln -s $GOPATH/bin/gopass $HOME/bin/pass

(defn single-gopass-setup
  [user-name
   user-home
   repo-name]
  (actions/remote-file
   (str user-home "/.password-store")
   :owner user-name
   :group user-name
   :link (str user-home "/repo/credential-store/" repo-name))
  (actions/remote-file
   (str user-home "/.config/gopass/config.yml")
   :literal true
   :content (selmer/render-file "gopass.yml.templ" {:user-name user-name})
   :mode "644"
   :owner user-name
   :group user-name))

(defn multi-gopass-setup
  [user-name
   user-home
   credential-store]
   (let [std-passwordstore (selmer/render-file "gopass.yml.templ" {:user-name user-name})
         passwordstorestomount (apply str (for [repo credential-store] (selmer/render-file "gopass_mount.yml.templ" {:repo-name (:repo-name repo)
                                                                                                                     :user-name user-name})))]
     (actions/directory 
      (str user-home "/.password-store")
      :owner user-name
      :group user-name)
     (actions/remote-file
      (str user-home "/.config/gopass/config.yml")
      :literal true
      :content (str std-passwordstore passwordstorestomount)
      :mode "644"
      :owner user-name
      :group user-name)))

(defn create-gopass-autocompletion-file
  [user-name
   user-home]
  (actions/remote-file
    (str user-home "/.bashrc.d/gopass.sh")
    :literal true
    :content (selmer/render-file "gopass.sh.templ" {})
    :mode "644"
    :owner user-name
    :group user-name))


(s/defn configure-gopass
  [facility :- s/Keyword
   user-name
   credential-store]
  (let [user-home (str "/home/" user-name)
        script (str "mkdir -p " user-home "/.config/gopass/ && touch " user-home "/.config/gopass/config.yml")]
    (actions/as-action
     (logging/info (str facility "-configure user: configure-gopass")))
    (actions/exec-script script)
    (create-gopass-autocompletion-file user-name user-home)
    (when (some? credential-store)
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
   credential-store
   settings]
  (when (contains? settings :install-password-store)
    (configure-password-store facility user-name))
  (when (contains? settings :install-gopass)
    (configure-gopass facility user-name credential-store)))
