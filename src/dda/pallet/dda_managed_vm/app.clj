; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

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
    [dda.pallet.commons.external-config :as ext-config]))

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

(s/defn ^:always-validate app-configuration :- DdaVmAppConfig
 [domain-config :- domain/DdaVmDomainConfig
  & options]
 (let [{:keys [group-key] :or {group-key infra/facility}} options]
   (mu/deep-merge
     (user/app-configuration (domain/user-config domain-config) :group-key group-key)
     ; TODO - only if install-git is selected
     (git/app-configuration (domain/vm-git-config domain-config) :group-key group-key)
     (serverspec/app-configuration (domain/vm-serverspec-config domain-config) :group-key group-key)
     {:group-specific-config
        {group-key (domain/infra-configuration domain-config)}})))

(s/defn ^:always-validate vm-group-spec
 [app-config :- DdaVmAppConfig]
 (group/group-spec
   app-config [(config-crate/with-config app-config)
               serverspec/with-serverspec
               user/with-user
               git/with-git
               with-dda-vm]))
