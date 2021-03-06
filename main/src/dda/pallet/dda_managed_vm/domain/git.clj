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
    [dda.pallet.dda-git-crate.domain :as git-domain]))

(def ServerIdentity git-domain/ServerIdentity)
(def Repository git-domain/Repository)
(def Repositories [Repository])
(def GitCredential git-domain/GitCredential)
(def GitCredentials git-domain/GitCredentials)
(def GitCredentialsResolved git-domain/GitCredentialsResolved)

(s/defn protocol-type
  [git-credentials :- GitCredentials
   host :- s/Str]
  (let [github-ssh (for [x git-credentials] (and (= (:host x) host) (= (:protocol x) :ssh)))]
     (if (and (not (nil? git-credentials))
              (some true? github-ssh))
        :ssh :https)))

(s/defn vm-git-config
 "Git repos for VM"
 [name :- s/Str
  email :- s/Str
  git-credentials :- GitCredentials
  git-signing-key :- s/Str
  desktop-wiki :- Repositories
  credential-store :- Repositories]
 (let [email (if (some? email) email (str name "@mydomain"))]
   {(keyword name)
    (merge
      {:user-email email}
      (when (some? git-credentials)
        {:credential git-credentials})
      (when (some? git-signing-key)
        {:signing-key git-signing-key})
      {:repo {:books
              [{:host "github.com"
                :orga-path "DomainDrivenArchitecture"
                :repo-name "ddaArchitecture"
                :protocol (protocol-type git-credentials, "github.com")
                :server-type :github}]}}
      {:synced-repo
       (merge
         {:credential-store (if (= credential-store nil) [] credential-store)}
         (when (some? desktop-wiki)
          {:desktop-wiki desktop-wiki}))}
      {})}))
