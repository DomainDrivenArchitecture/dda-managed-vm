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
    [dda.pallet.commons.secret :as secret]))

(def GitCredentials
  {(s/enum :gitblit :github) {:user s/Str
                              (s/optional-key :password) secret/Secret}})

(def GitCredentialsResolved
  (secret/create-resolved-schema GitCredentials))

(s/defn vm-git-config
 "Git repos for VM"
 [domain-config]
 (let [{:keys [user usage-type]} domain-config
       {:keys [name email git-credentials desktop-wiki credentials]
        :or {email (str name "@mydomain")}} user]
   (mu/deep-merge
     {:os-user (keyword name)
      :user-email email
      :repos {}}
     (when (contains? user :git-credentials)
       {:credentials git-credentials})
     (when (not (= usage-type :desktop-minimal))
       {:repos {:book
                ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"]
                :credentials
                ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}})
     (when (contains? user :credentials)
       {:repos {:credentials credentials}})
     (when (contains? user :desktop-wiki)
       {:synced-repos {:wiki desktop-wiki}}))))
