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

(ns dda.pallet.dda-managed-vm.infra
  (:require
   [clojure.tools.logging :as logging]
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.config.commons.user-home :as user-home]
   [dda.pallet.core.infra :as core-infra]
   [dda.pallet.dda-managed-vm.infra.basics :as basics]
   [dda.pallet.dda-managed-vm.infra.tightvnc :as tightvnc]
   [dda.pallet.dda-managed-vm.infra.office :as office]
   [dda.pallet.dda-managed-vm.infra.credential-management :as cm]
   [dda.pallet.dda-managed-vm.infra.browser :as browser]
   [dda.pallet.dda-managed-vm.infra.communication :as communication]
   [dda.pallet.dda-managed-vm.infra.desktop-wiki :as wiki]
   [dda.pallet.dda-managed-vm.infra.java :as java]
   [dda.pallet.dda-managed-vm.infra.vpn :as vpn]))

(def facility :dda-managed-vm)

(def Bookmarks browser/Bookmarks)

(def DdaVmConfig
  "The configuration for managed vms crate."
  {:vm-user s/Keyword
   (s/optional-key :tightvnc-server) tightvnc/Tightvnc
   (s/optional-key :bookmarks) browser/Bookmarks
   (s/optional-key :fakturama) office/FakturamaConfig
   (s/optional-key :credential-store) s/Any
   (s/optional-key :settings)
   (hash-set (apply s/enum
                    (clojure.set/union
                      basics/Settings
                      wiki/Settings
                      java/Settings
                      office/Settings
                      cm/Settings
                      browser/Settings
                      communication/Settings
                      vpn/Settings
                      #{:install-git})))})

(s/defn init
  "init package management"
  [facility :- s/Keyword
   config :- DdaVmConfig]
  (actions/as-action
    (logging/info (str facility "-init phase")))
  (let [{:keys [settings]} config]
    (cm/init-system facility settings)
    (wiki/init-system facility settings)
    (office/init-system facility settings)))

(s/defn install-system
  "install common used packages for vm"
  [facility :- s/Keyword
   config :- DdaVmConfig]
  (let [{:keys [settings tightvnc-server]} config
        contains-tightvnc? (contains? config :tightvnc-server)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (actions/package-manager :update)
     (basics/install-system facility settings)
     (office/install-system facility config)
     (communication/install-system facility settings)
     (wiki/install-system facility settings)
     (vpn/install-system facility settings)
     (cm/install-system facility settings)
     (java/install-system facility settings)
     (browser/install-system facility settings)
     (tightvnc/install-system facility contains-tightvnc?))))

(s/defn install-user
  "install the user space peaces in vm"
  [facility :- s/Keyword
   config :- DdaVmConfig]
  (let [{:keys [settings vm-user tightvnc-server]} config
        user-name (name vm-user)
        contains-tightvnc? (contains? config :tightvnc-server)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (tightvnc/install-user facility user-name contains-tightvnc? tightvnc-server))))

(s/defn configure-system
  "install the user space peaces in vm"
  [facility :- s/Keyword
   config :- DdaVmConfig]
  (let [{:keys [settings]} config]
   (basics/configure-system facility settings)))

(s/defn configure-user
  "install the user space peaces in vm"
  [facility :- s/Keyword
   config :- DdaVmConfig]
  (let [{:keys [vm-user settings bookmarks tightvnc-server credential-store]} config
        contains-bookmarks? (contains? config :bookmarks)
        contains-tightvnc? (contains? config :tightvnc-server)
        user-name (name vm-user)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (basics/configure-user facility user-name settings)
     (cm/configure-user facility user-name credential-store settings)
     (office/configure-user facility user-name settings)
     (browser/configure-user facility user-name contains-bookmarks? bookmarks)
     (tightvnc/configure-user facility user-name contains-tightvnc? tightvnc-server))))

(s/defmethod core-infra/dda-init facility
  [core-infra config]
  "dda managed vm: init routine"
  (init (:facility core-infra) config))

(s/defmethod core-infra/dda-install facility
  [core-infra config]
  "dda managed vm: install routine"
  (install-system (:facility core-infra) config)
  (install-user (:facility core-infra) config))

(s/defmethod core-infra/dda-configure facility
  [core-infra config]
  "dda managed vm: configure routine"
  (configure-system (:facility core-infra) config)
  (configure-user (:facility core-infra) config))

(def dda-vm-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-dda-vm
  (core-infra/create-infra-plan dda-vm-crate))
