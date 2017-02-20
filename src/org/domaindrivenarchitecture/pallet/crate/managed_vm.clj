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

(ns org.domaindrivenarchitecture.pallet.crate.managed-vm
  (:require
    [pallet.api :as api]
    [schema.core :as s]
    [clojure.tools.logging :as logging]
    [pallet.actions :as actions]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.core.dda-crate.config :as config]
    [org.domaindrivenarchitecture.pallet.crate.package :as dda-package]
    [org.domaindrivenarchitecture.pallet.crate.user :as user]
    [org.domaindrivenarchitecture.pallet.crate.user.os-user :as os-user]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.basics :as basics]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.tightvnc :as tightvnc]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.office :as office]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.convenience :as convenience]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.java :as java]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.test-vm :as test-vm]))
  
(def facility :dda-managed-vm)
    
(def DdaVmConfig
  "The configuration for managed vms crate." 
  {:vm-user s/Keyword
   (s/optional-key :bookmarks-download-url) s/Str
   (s/optional-key :tightvnc-server) {:user-password s/Str}
   (s/optional-key :settings) 
   (hash-set (s/enum :install-virtualbox-guest :install-libreoffice 
                     :install-open-jdk-8 :install-xfce-desktop
                     :install-linus-basics :install-git))
   })

(s/defn init
  "init package management"
  [app-name :- s/Str
   config :- DdaVmConfig]
  (actions/package-manager :update))

(s/defn install-system
  "install common used packages for vm"
  [config :- DdaVmConfig
   global-config]
  (let [settings (-> config :settings)
        user-key (:vm-user config)]
    (pallet.action/with-action-options 
      {:sudo-user "root"
       :script-dir "/root/"
       :script-env {:HOME (str "/root")}}
      (user/create-sudo-user (os-user/new-os-user-from-config user-key global-config))
      (when (contains? settings :install-git)
        (basics/install-git))
      (when (contains? settings :install-linus-basics)
        (basics/install-linus-basics))
      (when (contains? settings :install-xfce-desktop)
        (basics/install-xfce-desktop))
      (when (contains? settings :install-virtualbox-guest)
        (basics/install-virtualbox-guest-additions))
      (when (contains? config :tightvnc-server)
        (tightvnc/install-system-tightvnc-server config))
      (when (contains? settings :install-libreoffice)
        (office/install-libreoffice))
      (when (contains? settings :install-open-jdk-8)
        (java/install-open-jdk-8))
      )))

(s/defn install-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (pallet.action/with-action-options 
      {:sudo-user os-user-name
       :script-dir (str "/home/" os-user-name "/")
       :script-env {:HOME (str "/home/" os-user-name "/")}}
      (when (contains? config :bookmarks-download-url)
        (convenience/install-user-bookmarks os-user-name (-> config :bookmarks-download-url)))
      (when (contains? config :tightvnc-server)
        (tightvnc/install-user-tightvnc-server config)
        (tightvnc/install-user-vnc-tab-workaround config))
      ))
  )

(s/defn configure-system
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (pallet.action/with-action-options 
      {:sudo-user "root"
       :script-dir "/root/"
       :script-env {:HOME (str "/root")}}
      (when (contains? config :tightvnc-server)
              (tightvnc/configure-system-tightvnc-server config))
      (when (contains? settings :install-virtualbox-guest)
        (basics/configure-virtualbox-guest-additions config))
      )))

(s/defn configure-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :vm-user))
        settings (-> config :settings)]
    (pallet.action/with-action-options 
      {:sudo-user os-user-name
       :script-dir (str "/home/" os-user-name "/")
       :script-env {:HOME (str "/home/" os-user-name "/")}}
      (when (contains? config :tightvnc-server)
              (tightvnc/configure-user-tightvnc-server config))
      )))

(s/defn vm-test
  "test vm"
  [config :- DdaVmConfig]
  (test-vm/test-vm config))
 
(s/defmethod dda-crate/dda-settings facility 
  [dda-crate config]
  "dda managed vm: test routine"
  (test-vm/collect-facts config) 
 )

(s/defmethod dda-crate/dda-init facility 
  [dda-crate config]
  "dda managed vm: init routine"
  (let [app-name (name (:facility dda-crate))]
    (init app-name config)))

(s/defmethod dda-crate/dda-install facility 
  [dda-crate config]
  "dda managed vm: install routine"
  (let [global-config (config/get-global-config)]
    (install-system config global-config)
    (install-user config)))

(s/defmethod dda-crate/dda-configure facility 
  [dda-crate config]
  "dda managed vm: configure routine"
  (configure-user config)
  (configure-system config)
 )

(s/defmethod dda-crate/dda-test facility 
  [dda-crate config]
  "dda managed vm: test routine"
  (vm-test config)
 )

(def dda-vm-crate
  (dda-crate/make-dda-crate
      :facility facility
      :version [0 0 0]))

(def with-dda-vm
  (dda-crate/create-server-spec dda-vm-crate))
