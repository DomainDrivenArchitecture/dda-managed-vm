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
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.core.app :as core-app]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [dda.pallet.dda-git-crate.app :as git]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.dda-serverspec-crate.app :as serverspec]
    [dda.pallet.dda-managed-vm.infra :as infra]
    [dda.pallet.dda-managed-vm.domain :as domain]))

(def with-dda-vm infra/with-dda-vm)

(def DdaVmDomainConfig domain/DdaVmDomainConfig)

(def DdaVmDomainResolvedConfig domain/DdaVmDomainResolvedConfig)

(def InfraResult domain/InfraResult)

(def DdaVmAppConfig
  {:group-specific-config
   {s/Keyword (merge InfraResult
                     git/InfraResult
                     user/InfraResult
                     serverspec/InfraResult)}})

(s/defn ^:always-validate
  app-configuration-resolved :- DdaVmAppConfig
  [resolved-domain-config :- DdaVmDomainResolvedConfig
   & options]
  (let [{:keys [group-key] :or {group-key infra/facility}} options]
    (mu/deep-merge
      (user/app-configuration-resolved (domain/user-config resolved-domain-config) :group-key group-key)
      (git/app-configuration-resolved (domain/vm-git-config resolved-domain-config) :group-key group-key)
      (serverspec/app-configuration (domain/vm-serverspec-config resolved-domain-config) :group-key group-key)
      {:group-specific-config
         {group-key (domain/infra-configuration resolved-domain-config)}})))

(s/defn ^:always-validate
  app-configuration :- DdaVmAppConfig
  [domain-config :- DdaVmDomainConfig
   & options]
  (let [resolved-domain-config (secret/resolve-secrets domain-config DdaVmDomainConfig)]
    (apply app-configuration-resolved resolved-domain-config options)))

(s/defmethod ^:always-validate
  core-app/group-spec infra/facility
  [crate-app
   domain-config :- DdaVmDomainResolvedConfig]
  (let [app-config (app-configuration-resolved domain-config)]
    (core-app/pallet-group-spec
      app-config [(config-crate/with-config app-config)
                  serverspec/with-serverspec
                  user/with-user
                  git/with-git
                  with-dda-vm])))

(def crate-app (core-app/make-dda-crate-app
                  :facility infra/facility
                  :domain-schema DdaVmDomainConfig
                  :domain-schema-resolved DdaVmDomainResolvedConfig
                  :default-domain-file "example-vm.edn"))
