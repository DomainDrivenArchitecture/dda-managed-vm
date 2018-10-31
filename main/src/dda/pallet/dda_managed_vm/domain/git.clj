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
(ns dda.pallet.dda-managed-vm.domain.git
  (:require
    [schema.core :as s]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.dda-git-crate.domain :as git-domain]))

(def ServerIdentity git-domain/ServerIdentity)
(def Repository git-domain/Repository)
(def Repositories [Repository])
(def GitCredential git-domain/GitCredential)
(def GitCredentials git-domain/GitCredentials)
(def GitCredentialsResolved git-domain/GitCredentialsResolved)

(s/defn github-protocol-type
  [git-credentials :- GitCredentials]
  (let [github-ssh (for [x git-credentials] (and (= (:host x) "github.com") (= (:protocol x) :ssh)))]
     (if (and (not (nil? git-credentials))
              (some true? github-ssh))
        :ssh :https)))

(s/defn vm-git-config
 "Git repos for VM"
 [name :- s/Str
  email :- s/Str
  git-credentials :- GitCredentials
  desktop-wiki :- Repositories
  credential-store :- Repositories]
 (let [email (if (some? email) email (str name "@mydomain"))
       protocol-type (protocol-type git-credentials)]
   {(keyword name)
    (merge
      {:user-email email}
      (when (some? git-credentials)
        {:credential git-credentials})
      {:repo {:books
              [{:host "github.com"
                :orga-path "DomainDrivenArchitecture"
                :repo-name "ddaArchitecture"
                :protocol protocol-type
                :server-type :github}]}}
      {:synced-repo
       (merge
         {:credential-store
          (into [] (concat
                     [{:host "github.com"
                       :orga-path "DomainDrivenArchitecture"
                       :repo-name "password-store-for-teams"
                       :protocol protocol-type
                       :server-type :github}]
                     (when (some? credential-store)
                       credential-store)))}
         (when (some? desktop-wiki)
          {:desktop-wiki desktop-wiki}))}
      {})}))
