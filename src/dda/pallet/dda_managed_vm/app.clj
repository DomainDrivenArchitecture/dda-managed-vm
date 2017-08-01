; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.domain
  (:require
    [schema.core :as s]
    [dda.cm.group :as group]
    [dda.config.commons.map-utils :as map-utils]
    [dda.pallet.dda-config-crate.infra :as config-crate]
    [org.domaindrivenarchitecture.pallet.crate.backup :as backup]
    [dda.pallet.dda-git-crate.app :as git]
    [dda.pallet.dda-user-crate.app :as user]
    [dda.pallet.dda-serverspec-crate.app :as test]
    [dda.pallet.dda-managed-vm.infra :as infra]
    [dda.pallet.dda-managed-vm.domain :as domain]))

(def DdaVmAppConfig
  {:group-specific-config
   {s/Keyword {infra/facility infra/DdaVmConfig}}})

(s/defn ^:allways-validate create-app-configuration :- DdaVmAppConfig
  [config :- infra/DdaVmConfig
   group-key :- s/Keyword]
  {:group-specific-config
     {group-key config}})

(def with-dda-vm infra/with-dda-vm)

(defn app-configuration
 [user-config vm-config & {:keys [group-key] :or {group-key :dda-vm-group}}]
 (s/validate domain/DdaVmConfig vm-config)
 (mu/deep-merge
   (user/app-configuration user-config :group-key group-key)
   (git/app-configuration (domain/vm-git-config vm-config) :group-key group-key)
   (test/app-configuration (domain/vm-test-config vm-config) :group-key group-key)
   (create-app-configuration (domain/infra-configuration vm-config) :group-key group-key)))

(s/defn ^:always-validate servertest-group-spec
 [app-config :- DdaVmAppConfig]
 (group/group-spec
   app-config [(config-crate/with-config app-config)
               server-test-crate/with-servertest
               user-crate/with-user
               git-crate/with-git
               with-dda-vm]))
