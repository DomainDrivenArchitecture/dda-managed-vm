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
    [dda.pallet.dda-managed-vm.domain :as domain]))

(def with-dda-vm infra/with-dda-vm)

(def InfraResult domain/InfraResult)

(def DdaVmAppConfig
  {:group-specific-config
   {s/Keyword (merge InfraResult
                     git/InfraResult
                     user/InfraResult
                     serverspec/InfraResult)}})

(s/defn ^:allways-validate create-app-configuration :- DdaVmAppConfig
  [config :- infra/DdaVmConfig
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(defn app-configuration
 [user-config vm-config & {:keys [group-key] :or {group-key :dda-vm-group}}]
 (s/validate domain/DdaVmDomainConfig vm-config)
 (mu/deep-merge
   (user/app-configuration user-config :group-key group-key)
   ; TODO - only if install-git is selected
   (git/app-configuration (domain/vm-git-config vm-config) :group-key group-key)
   (serverspec/app-configuration (domain/vm-serverspec-config vm-config) :group-key group-key)
   (create-app-configuration (domain/infra-configuration vm-config) group-key)))

(s/defn ^:always-validate vm-group-spec
 [app-config :- DdaVmAppConfig]
 (group/group-spec
   app-config [(config-crate/with-config app-config)
               serverspec/with-serverspec
               user/with-user
               git/with-git
               with-dda-vm]))
