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
   [dda.pallet.dda-managed-vm.infra.java :as java]))

(def facility :dda-managed-vm)

(def Bookmarks browser/Bookmarks)

(def DdaVmConfig
  "The configuration for managed vms crate."
  {:vm-user s/Keyword
   (s/optional-key :tightvnc-server) {:user-password s/Str}
   (s/optional-key :bookmarks) Bookmarks
   (s/optional-key :fakturama) office/FakturamaConfig
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
                      #{:install-git})))})

(s/defn init
  "init package management"
  [app-name :- s/Str
   config :- DdaVmConfig]
  (actions/package-manager :update))

(s/defn install-system
  "install common used packages for vm"
  [config :- DdaVmConfig]
  (let [settings (-> config :settings)
        user-key (:vm-user config)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (basics/install-system facility settings)
     (office/install-system facility config)
     (communication/install-system facility settings)
     (wiki/install-system facility settings)
     (when (contains? settings :install-chromium)
       (actions/as-action
         (logging/info (str facility "-install system: chromium")))
       (browser/install-chromium))
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-install system: tightvnc")))
       (tightvnc/install-system-tightvnc-server config))
     (when (contains? settings :install-password-store)
       (actions/as-action
        (logging/info (str facility "-install system: password-store")))
       (cm/install-password-store)))
    (when (contains? settings :install-gopass)
      (actions/as-action
       (logging/info (str facility "-install system: install-gopass")))
      (cm/install-gopass)
     (when (contains? settings :install-open-jdk-8)
       (actions/as-action
        (logging/info (str facility "-install system: openjdk 8")))
       (java/install-open-jdk-8)))
    (when (contains? settings :install-open-jdk-11)
      (actions/as-action
       (logging/info (str facility "-install system: openjdk 11")))
      (java/install-open-jdk-11))))

(s/defn install-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-install user: tightvnc")))
       (tightvnc/install-user-tightvnc-server config)
       (tightvnc/install-user-vnc-tab-workaround config))
     (when (contains? config :tightvnc-server)
       (tightvnc/configure-user-tightvnc-server-script os-user-name config)))))

(s/defn configure-system
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [settings (-> config :settings)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-configure system: tightvnc")))
       (tightvnc/configure-system-tightvnc-server config))
     (basics/configure-system facility config))))

(s/defn configure-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [{:keys [vm-user settings bookmarks]} config
        user-name (name vm-user)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (when bookmarks
       (actions/as-action
        (logging/info (str facility "-configure user: bookmarks")))
       (browser/configure-user-bookmarks user-name bookmarks))
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-configure user: tightvnc")))
       (tightvnc/configure-user-tightvnc-server-script user-name config))
     (when (contains? settings :install-password-store)
       (actions/as-action
        (logging/info (str facility "-configure user: passwordstore")))
       (cm/configure-password-store user-name)))
    (when (contains? settings :install-gopass)
      (actions/as-action
       (logging/info (str facility "-configure user: gopass")))
      (cm/configure-gopass user-name))))

(s/defmethod core-infra/dda-init facility
  [core-infra config]
  "dda managed vm: init routine"
  (let [app-name (name (:facility core-infra))]
    (init app-name config)))

(s/defmethod core-infra/dda-install facility
  [core-infra config]
  "dda managed vm: install routine"
  (install-system config)
  (install-user config))

(s/defmethod core-infra/dda-configure facility
  [core-infra config]
  "dda managed vm: configure routine"
  (configure-user config)
  (configure-system config))

(def dda-vm-crate
  (core-infra/make-dda-crate-infra
   :facility facility))

(def with-dda-vm
  (core-infra/create-infra-plan dda-vm-crate))
