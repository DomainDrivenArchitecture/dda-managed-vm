; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.domain
  (:require
    [schema.core :as s]
    [dda.config.commons.map-utils :as map-utils]
    [dda.pallet.dda-git-crate.domain :as git]
    [dda.pallet.dda-serverspec-crate.domain :as serverspec]
    [org.domaindrivenarchitecture.pallet.crate.backup :as backup]
    [dda.pallet.dda-managed-vm.infra :as infra]))

(def DdaVmDomainConfig
  "The convention configuration for managed vms crate."
  {:vm-user s/Keyword
   :platform (s/enum :virtualbox :aws)
   (s/optional-key :user-email) s/Str})

(def InfraResult {infra/facility infra/DdaVmConfig})

(s/defn ^:always-validate vm-git-config :- git/InfraResult
 "Git repos for VM"
 [vm-config :- DdaVmDomainConfig]
 (let [{:keys [vm-user user-email]} vm-config
       used-email (if (contains? vm-config :user-email)
                      user-email
                      (str (name vm-user) "@domain"))]
   (git/infra-configuration
     {:os-user vm-user
      :user-email used-email
      :repos {:dda-book
              ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"]}})))

(s/defn ^:always-validate vm-serverspec-config :- serverspec/InfraResult
 "serverspec for VM"
 [vm-config :- DdaVmDomainConfig]
 (let [{:keys [platform]} vm-config]
   (cond
     (= platform :aws)
     (serverspec/infra-configuration
      {:package {:xfce4 {:installed? true}}
       :netstat {:Xtightvnc {:port "5901"}}})
     :default
     (serverspec/infra-configuration {}))))

(s/defn ^:always-validate vm-backup-config
  "Managed vm crate default configuration"
  [vm-config :- DdaVmDomainConfig]
  (let [{:keys [vm-user]} vm-config]
    {:backup-name "managed-vm"
     :script-path "/usr/lib/dda-backup/"
     :gens-stored-on-source-system 1
     :elements [{:type :file-compressed
                 :name "user-home"
                 :root-dir (str "/home/" (name vm-user))
                 :subdir-to-save ".ssh .mozilla"}]
     :backup-user {:name "dataBackupSource"
                   :encrypted-passwd "WIwn6jIUt2Rbc"}}))

(s/defn ^:always-validate meissa-convention :- InfraResult
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainConfig]
  (let [{:keys [vm-user platform]} domain-config]
    {infra/facility
      (merge
        {:vm-user (name (vm-user))}
        (cond
          (= platform :virtualbox)
          {:settings
            #{:install-virtualbox-guest
              :install-libreoffice :install-open-jdk-8
              :install-linus-basics :install-git}}
          (= platform :aws)
          {:tightvnc-server {:user-password "test"
                             :settings
                             #{:install-xfce-desktop :install-open-jdk-8
                               :install-linus-basics :install-git}}}))}))
