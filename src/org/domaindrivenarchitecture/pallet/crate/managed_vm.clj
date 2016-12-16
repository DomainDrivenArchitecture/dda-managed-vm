; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

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
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.office :as office]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.convenience :as convenience]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm.java :as java]))
  
(def facility :dda-managed-vm)
(def version  [0 3 3])
    
(def DdaVmConfig
  "The configuration for managed vms crate." 
  {:ide-user s/Keyword
   (s/optional-key :bookmarks-download-url) s/Str
   :settings 
   #{(s/optional-key :install-virtualbox-guest) (s/optional-key :install-libreoffice) :install-open-jdk-8}})

(defn default-vm-backup-config
  "Managed vm crate default configuration"
  [user-name]
  {:backup-name "managed-vm"
   :script-path "/usr/lib/dda-backup/"
   :gens-stored-on-source-system 1
   :elements [{:type :file-compressed
               :name "user-home"
               :root-dir (str "/home/" user-name)
               :subdir-to-save ".ssh .mozilla"}]
   :backup-user {:name "dataBackupSource"
                 :encrypted-passwd "WIwn6jIUt2Rbc"}})

(defn default-vm-config
  "Managed vm crate default configuration"
  [user-name]
  {:ide-user user-name
   :settings #{}})

(s/defmethod dda-crate/merge-config facility
  [dda-crate 
   partial-config]
  "merges the partial config with default config and
   ensures that resulting config is valid"  
  (let [user-key (:ide-user partial-config)
        user-name (name user-key)]
    (s/validate 
      DdaVmConfig
      (map-utils/deep-merge 
        (default-vm-config (name user-key))
        partial-config))))

(s/defn init
  "init package management"
  [app-name :- s/Str
   config :- DdaVmConfig]
  (actions/package-manager :update))

(s/defn install-system
  "install common used packages for vm"
  [config :- DdaVmConfig]
  (let [settings (-> config :settings)]
    (pallet.action/with-action-options 
      {:sudo-user "root"
       :script-dir "/root/"
       :script-env {:HOME (str "/root")}}
      (when (contains? settings :install-virtualbox-guest)
        (basics/install-virtualbox-guest-additions))
      (when (contains? settings :install-libreoffice)
        (office/install-libreoffice))
      (when (contains? settings :install-open-jdk-8)
        (java/install-open-jdk-8))
      )))

(s/defn install-user
  "install the user space peaces in vm"
  [config :- DdaVmConfig]
  (let [os-user-name (name (-> config :ide-user))]
    (pallet.action/with-action-options 
      {:sudo-user os-user-name
       :script-dir (str "/home/" os-user-name "/")
       :script-env {:HOME (str "/home/" os-user-name "/")}}
      (when (contains? config :bookmarks-download-url)
        (convenience/install-user-bookmarks os-user-name (-> config :bookmarks-download-url))))))
    

(s/defmethod dda-crate/dda-init facility 
  [dda-crate partial-effective-config]
  "dda managed vm: init routine"
  (let [config (dda-crate/merge-config dda-crate partial-effective-config)
        app-name (name (:facility dda-crate))]
    (init app-name config)))

(s/defmethod dda-crate/dda-install facility 
  [dda-crate partial-effective-config]
  "dda managed vm: install routine"
  (let [config (dda-crate/merge-config dda-crate partial-effective-config)
        user-key (:ide-user config)
        user-name (name user-key)
        app-name (name (:facility dda-crate))
        global-config (config/get-global-config)
        git-user-name (:git-user-name config)
        bookmarks-download-url (:bookmarks-download-url config)]
    (user/create-sudo-user (os-user/new-os-user-from-config user-key global-config))
    (install-system config)
    (install-user config)))

(s/defmethod dda-crate/dda-configure facility 
  [dda-crate partial-effective-config]
  "dda managed vm: configure routine"
  (let [config (dda-crate/merge-config dda-crate partial-effective-config)]
    ))

(def dda-vm-crate
  (dda-crate/make-dda-crate
      :facility facility
      :version version))

(def with-dda-vm
  (dda-crate/create-server-spec dda-vm-crate))
