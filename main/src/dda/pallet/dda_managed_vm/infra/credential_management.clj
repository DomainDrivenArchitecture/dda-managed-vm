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
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.config.commons.user-home :as user-env]))

(def Settings
  (hash-set :install-password-store :install-gopass))

(defn install-gpg
  []
  (actions/packages :aptitude ["rng-tools" "gnupg2"]))

(defn install-password-store
  []
  (install-gpg)
  (actions/packages :aptitude ["pass"]))

(defn configure-password-store
  [user-name]
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/remote-file
     (str user-home "/.demo-pass")
     :owner user-name
     :group user-name
     :link (str user-home "/repo/password-store/password-store-for-teams"))
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

(defn install-gopass
  []
  (install-gpg)
  (actions/package-source "gopass"
    :aptitude
    {:url "deb https://dl.bintray.com/gopasspw/gopass"
     :release "bionic"
     :scopes ["main"]
     :key-url "https://api.bintray.com/orgs/gopasspw/keys/gpg/public.key"})
  (actions/package-manager :update)
  (actions/packages :aptitude ["gopass"]))
  ;au BufNewFile,BufRead /dev/shm/gopass.* setlocal noswapfile nobackup noundofile
  ;ln -s $GOPATH/bin/gopass $HOME/bin/pass

(defn configure-gopass
  [user-name]
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/directory (str user-home "/.password-store")
      :owner user-name :group user-name)
    (actions/remote-file
     (str user-home "/.password-store/demo")
     :owner user-name
     :group user-name
     :link (str user-home "/repo/password-store/password-store-for-teams"))
    ;gopass init
    ;gopass mounts add dem (str user-home "/repo/password-store/password-store-for-teams"))
    (actions/remote-file
     (str user-home "/.bashrc.d/gopass.sh")
     :literal true
     :content "# autocompletion for gopass
source <(gopass completion bash)
"
     :mode "644"
     :owner user-name
     :group user-name)))
