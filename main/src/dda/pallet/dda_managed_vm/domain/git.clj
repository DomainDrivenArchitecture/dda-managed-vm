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
    [dda.pallet.commons.secret :as secret])) ;TODO

(def ServerIdentity ;TODO von gitcrate von domain
  {:host s/Str                                 ;identifyer for repo matching
   (s/optional-key :port) s/Num                ;identifyer for repo matching, defaults to 22 or 443 based on protocol
   :protocol (s/enum :ssh :https)})

(def Repository ;TODO von gitcrate von domain
  (merge
    ServerIdentity
    {(s/optional-key :orga-path) s/Str
     :repo-name s/Str
     :server-type (s/enum :gitblit :github :gitlab)}))

(def Repositories [Repository])

(def GitCredential ;TODO von gitcrate von domain
  (merge
     ServerIdentity
     {:user-name secret/Secret                     ;needed for none-public access
      (s/optional-key :password) secret/Secret}))  ;needed for none-public & none-key access

(def GitCredentials [GitCredential]) ;TODO von gitcrate von domain

(def GitCredentialsResolved ;TODO von gitcrate von domain
  (secret/create-resolved-schema GitCredential))

(s/defn vm-git-config
 "Git repos for VM"
 [name :- s/Str
  email :- s/Str
  git-credentials :- GitCredentials
  desktop-wiki :- Repositories
  credential-store :- Repositories]
 (let [email (if (some? email) email (str name "@mydomain"))
       github-ssh (for [x git-credentials] (and (= (:host x) "github.com") (= (:protocol x) :ssh)))
       protocol-type (if github-ssh :ssh :https)]
   {(keyword name)
    (merge
      {:user-email email}
      (when (some? git-credentials)
        {:credential git-credentials})
      {:repo {:books
              [{:host "github.com"
                :orga-path "DomainDrivenArchitecture"
                :repo-name "ddaArchitecture"
                :protocol protocol-type ;TODO own fkt
                :server-type :github}]}}
      {:synced-repo
       (merge
         {:credential-store
          [{:host "github.com"
            :orga-path "DomainDrivenArchitecture"
            :repo-name "password-store-for-teams"
            :protocol protocol-type
            :server-type :github}]} ;TODO add credentials
         (when (some? desktop-wiki)
          {:wiki desktop-wiki}))}
      {})}))
