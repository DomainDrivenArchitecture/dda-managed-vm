; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns org.domaindrivenarchitecture.pallet.convention.managed-vm
  (:require
    [schema.core :as s]
    [org.domaindrivenarchitecture.config.commons.map-utils :as map-utils]
    [org.domaindrivenarchitecture.pallet.crate.backup :as backup]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm :as crate]))

(def DdaVmConventionConfig
  "The convention configuration for managed vms crate." 
  {:ide-user s/Keyword
   :platform (s/enum :virtualbox :aws :other)})

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
   :settings #{:install-virtualbox-guest :install-libreoffice 
               :install-open-jdk-8}})

(s/defn meissa-convention :- crate/DdaVmConfig
  [convention-config :- DdaVmConventionConfig]
  (let [platform (:platform convention-config)]
    (map-utils/deep-merge 
      {:ide-user (:ide-user convention-config)}
      (cond 
        (= platform :virtualbox) {:settings #{:install-virtualbox-guest 
                                          :install-libreoffice :install-open-jdk-8}}
        (= platform :aws) {:tightvnc-server {:user-password "test"}
                           :settings #{:install-xfce-desktop :install-tightvnc-server 
                            :install-libreoffice :install-open-jdk-8 :install-linus-basics}}
        (= platform :other) {:settings #{:install-libreoffice :install-open-jdk-8}})        
      ))
  )
           