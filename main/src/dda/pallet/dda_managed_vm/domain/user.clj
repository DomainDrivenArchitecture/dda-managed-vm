; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
(ns dda.pallet.dda-managed-vm.domain.user
  (:require
   [schema.core :as s]
   [clojure.string :as str]))

(defn authorized-keys
  [user]
  (let [{:keys [ssh]} user]
    (if (contains? user :ssh)
      {:ssh-authorized-keys [(:ssh-public-key ssh)]}
      {})))

(defn ssh-personal-key
  [user]
  (let [{:keys [ssh]} user]
    (if (contains? user :ssh)
      {:ssh-key {:public-key (:ssh-public-key ssh)
                 :private-key (:ssh-private-key ssh)}}
      {})))

(defn gpg
  [user]
  (let [{:keys [gpg]} user]
    (if (contains? user :gpg)
      {:gpg {:trusted-key {:public-key (:gpg-public-key gpg)
                           :private-key (:gpg-private-key gpg)
                           :passphrase (:gpg-passphrase gpg)}}}
      {})))
