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
(ns dda.pallet.dda-managed-vm.app.instantiate-aws
  (:require
    [clojure.inspector :as inspector]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.compute :as compute]
    [dda.pallet.commons.encrypted-credentials :as crypto]
    [dda.cm.operation :as operation]
    [dda.cm.aws :as cloud-target]
    [dda.config.commons.user-env :as user-env]
    [dda.pallet.dda-managed-vm.app :as app]))

(def ssh-pub-key
  (user-env/read-ssh-pub-key-to-config))

(def user-config
   {:user-name {:encrypted-password  "xxx"
                :authorized-keys [ssh-pub-key]}})

(def vm-config
  {:vm-user :user-name
   :platform :aws
   :user-email "user-name@mydomain.org"})

(defn integrated-group-spec [count]
  (merge
    (app/vm-group-spec (app/app-configuration user-config vm-config))
    (cloud-target/node-spec "jem")
    {:count count}))

(defn converge-install
  ([count]
   (operation/do-converge-install (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase count]
   (operation/do-converge-install (cloud-target/provider key-id key-passphrase) (integrated-group-spec count))))

(defn server-test
  ([count]
   (operation/do-server-test (cloud-target/provider) (integrated-group-spec count)))
  ([key-id key-passphrase count]
   (operation/do-server-test (cloud-target/provider key-id key-passphrase) (integrated-group-spec count))))
