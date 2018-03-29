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
(ns dda.pallet.dda-managed-vm.infra.passwordstore
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    [dda.config.commons.ssh-key :as user-env]))

(defn install-password-store
  []
  (actions/package "pass")
  (actions/package "gnupg2"))

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
