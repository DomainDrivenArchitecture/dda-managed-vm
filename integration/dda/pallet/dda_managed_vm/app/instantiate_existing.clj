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
(ns dda.pallet.dda-managed-vm.app.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [pallet.repl :as pr]
    [dda.pallet.commons.session-tools :as session-tools]
    [dda.pallet.commons.pallet-schema :as ps]
    [dda.cm.existing :as existing]
    [dda.cm.operation :as operation]
    [dda.config.commons.user-env :as user-env]
    [dda.pallet.dda-managed-vm.app :as app]))

(def provisioning-ip
  "192.168.56.104")

(def provisioning-user
  {:login "initial"
   :password "secure1234"})

(def ssh-pub-key
  (user-env/read-ssh-pub-key-to-config))

(def user-config
   {:user-name {:encrypted-password  "xxx"
                :authorized-keys [ssh-pub-key]}})

(def vm-config
  {:vm-user :user-name
   :platform :virtualbox
   :user-email "user-name@mydomain.org"})

(defn integrated-group-spec []
 (merge
   (app/vm-group-spec (app/app-configuration user-config vm-config))
   (existing/node-spec provisioning-user)))

(defn provider []
  (existing/provider provisioning-ip "node-id" "dda-vm-group"))

(defn install
  ([]
   (pr/session-summary
    (operation/do-apply-install  (provider) (integrated-group-spec)))))

(defn test
  ([]
   (pr/session-summary
    (operation/do-server-test  (provider) (integrated-group-spec)))))
