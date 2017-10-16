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
   [pallet.api :as api]
   [schema.core :as s]
   [clojure.tools.logging :as logging]
   [pallet.actions :as actions]
   [dda.config.commons.map-utils :as map-utils]
   [dda.pallet.core.dda-crate :as dda-crate]
   [dda.pallet.dda-managed-vm.infra.basics :as basics]
   [dda.pallet.dda-managed-vm.infra.tightvnc :as tightvnc]
   [dda.pallet.dda-managed-vm.infra.office :as office]
   [dda.pallet.dda-managed-vm.infra.passwordstore :as password-store]
   [dda.pallet.dda-managed-vm.infra.mozilla :as mozilla]
   [dda.pallet.dda-managed-vm.infra.java :as java]))

(def facility :dda-managed-vm)
(def version  [0 0 0])

(def DdaVmConfig
  "The configuration for managed vms crate."
  {:vm-user s/Keyword
   (s/optional-key :tightvnc-server) {:user-password s/Str}
   (s/optional-key :bookmarks) mozilla/Bookmarks
   (s/optional-key :settings)
   (hash-set (s/enum :install-virtualbox-guest :install-libreoffice
                     :install-open-jdk-8 :install-xfce-desktop
                     :install-analysis :install-git :install-password-store))})

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
     (actions/as-action
      (logging/info (str facility "-install system: create user " user-key)))
     (when (contains? settings :install-analysis)
       (actions/as-action
        (logging/info (str facility "-install system: analysis tools")))
       (basics/install-analysis))
     (when (contains? settings :install-xfce-desktop)
       (actions/as-action
        (logging/info (str facility "-install system: xfce")))
       (basics/install-xfce-desktop))
     (when (contains? settings :install-virtualbox-guest)
       (actions/as-action
        (logging/info (str facility "-install system: virtualbox-guest")))
       (basics/install-virtualbox-guest-additions))
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-install system: tightvnc")))
       (tightvnc/install-system-tightvnc-server config))
     (when (contains? settings :install-libreoffice)
       (actions/as-action
        (logging/info (str facility "-install system: libreoffice")))
       (office/install-libreoffice))
     (when (contains? settings :install-password-store)
       (actions/as-action
        (logging/info (str facility "-install system: password-store")))
       (password-store/install-password-store))
     (when (contains? settings :install-open-jdk-8)
       (actions/as-action
        (logging/info (str facility "-install system: openjdk")))
       (java/install-open-jdk-8)))))

(s/defn install-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (when (contains? config :tightvnc-server)
      (actions/as-action
       (logging/info (str facility "-install user: tightvnc")))
      (tightvnc/install-user-tightvnc-server config)
      (tightvnc/install-user-vnc-tab-workaround config))
    (pallet.action/with-action-options
     {:sudo-user os-user-name
      :script-dir (str "/home/" os-user-name "/")
      :script-env {:HOME (str "/home/" os-user-name "/")}}
     (when (contains? config :tightvnc-server)
       (tightvnc/configure-user-tightvnc-server-script config)))))

(s/defn configure-system
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (pallet.action/with-action-options
     {:sudo-user "root"
      :script-dir "/root/"
      :script-env {:HOME (str "/root")}}
     (when (contains? settings :install-password-store)
      (password-store/configure-password-store os-user-name))
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-configure system: tightvnc")))
       (tightvnc/configure-system-tightvnc-server config))
     (when (contains? settings :install-virtualbox-guest)
       (actions/as-action
         (logging/info (str facility "-configure system: tightvnc")))
       (basics/configure-virtualbox-guest-additions config)))))

(s/defn configure-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [{:keys [vm-user settings bookmarks]} config
        user-name (name vm-user)]
    (when bookmarks
      (actions/as-action
       (logging/info (str facility "-configure user: bookmarks")))
      (mozilla/configure-user-bookmarks user-name bookmarks))
    (pallet.action/with-action-options
     {:sudo-user user-name
      :script-dir (str "/home/" user-name "/")
      :script-env {:HOME (str "/home/" user-name "/")}}
     (when (contains? config :tightvnc-server)
       (actions/as-action
        (logging/info (str facility "-configure user: tightvnc")))
       (tightvnc/configure-user-tightvnc-server-script config)))))

(s/defmethod dda-crate/dda-init facility
  [dda-crate config]
  "dda managed vm: init routine"
  (let [app-name (name (:facility dda-crate))]
    (init app-name config)))

(s/defmethod dda-crate/dda-install facility
  [dda-crate config]
  "dda managed vm: install routine"
  (install-system config)
  (install-user config))

(s/defmethod dda-crate/dda-configure facility
  [dda-crate config]
  "dda managed vm: configure routine"
  (configure-user config)
  (configure-system config))

(def dda-vm-crate
  (dda-crate/make-dda-crate
   :facility facility
   :version version))

(def with-dda-vm
  (dda-crate/create-server-spec dda-vm-crate))
