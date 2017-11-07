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
(ns dda.pallet.dda-managed-vm.app
  (:require
    [schema.core :as s]
    [dda.cm.group :as group]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.app :as git]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.dda-serverspec-crate.app :as serverspec]
    [dda.pallet.dda-managed-vm.infra :as infra]
    [dda.pallet.dda-managed-vm.domain :as domain]
    [dda.pallet.commons.external-config :as ext-config]
    [dda.pallet.dda-managed-vm.app.secret-resolver :as secret-resolver]))

(def with-dda-vm infra/with-dda-vm)

(def InfraResult domain/InfraResult)

(def DdaVmAppConfig
  {:group-specific-config
   {s/Keyword (merge InfraResult
                     git/InfraResult
                     user/InfraResult
                     serverspec/InfraResult)}})

(s/defn ^:always-validate load-domain :- domain/DdaVmDomainConfig
  [file-name :- s/Str]
  (ext-config/parse-config file-name))

(s/defn resolve-secrets :- domain/DdaVmDomainResolvedConfig
  [domain-config :- domain/DdaVmDomainConfig]
  (let [{:keys [user type]} domain-config
        {:keys [ssh gpg]} user]
    {:type type
     :user (merge
             user
             {:password (secret-resolver/resolve-secret (:password user))}
             (if (contains? user :ssh)
              {:ssh {:ssh-public-key (secret-resolver/resolve-secret (:ssh-public-key ssh))
                     :ssh-private-key (secret-resolver/resolve-secret (:ssh-private-key ssh))}}
              {})
             (if (contains? user :gpg)
              {:gpg {:gpg-public-key (secret-resolver/resolve-secret (:gpg-public-key gpg))
                     :gpg-private-key (secret-resolver/resolve-secret (:gpg-private-key gpg))
                     :gpg-passphrase (secret-resolver/resolve-secret (:gpg-passphrase gpg))}}
              {}))}))

(s/defn ^:always-validate app-configuration :- DdaVmAppConfig
 [domain-config :- domain/DdaVmDomainConfig
  & options]
 (let [{:keys [group-key] :or {group-key infra/facility}} options
       resolved-domain-config (resolve-secrets domain-config)
       {:keys [type]} resolved-domain-config]
   (mu/deep-merge
     (user/app-configuration (domain/user-config resolved-domain-config) :group-key group-key)
     (if (= type :desktop-minimal)
       {}
       (git/app-configuration (domain/vm-git-config resolved-domain-config) :group-key group-key))
     (serverspec/app-configuration (domain/vm-serverspec-config resolved-domain-config) :group-key group-key)
     {:group-specific-config
        {group-key (domain/infra-configuration resolved-domain-config)}})))

(s/defn ^:always-validate vm-group-spec
 [app-config :- DdaVmAppConfig]
 (group/group-spec
   app-config [(config-crate/with-config app-config)
               serverspec/with-serverspec
               user/with-user
               git/with-git
               with-dda-vm]))
