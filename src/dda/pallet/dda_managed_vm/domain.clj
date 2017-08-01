; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.domain
  (:require
    [schema.core :as s]
    [dda.config.commons.map-utils :as map-utils]
    [dda.pallet.dda-git-crate.domain :as git]
    [org.domaindrivenarchitecture.pallet.crate.backup :as backup]
    [dda.pallet.dda-managed-vm.infra :as infra]))

(def DdaVmDomainConfig
  "The convention configuration for managed vms crate."
  {:vm-user s/Keyword
   :platform (s/enum :virtualbox :aws)})

(def InfraResult {infra/facility infra/DdaVmConfig})

(s/defn vm-git-config :- git-infra/
 "Git repos for VM"
 [{:keys [vm-user]} vm-config]
 (git/infra-configuration
   {:os-user vm-user
    :user-email (str (name vm-user) "@domain")
    :repos {:dda-book
            ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"]}}))

(defn vm-backup-config
  "Managed vm crate default configuration"
  [{:keys [vm-user]} vm-config]
  {:backup-name "managed-vm"
   :script-path "/usr/lib/dda-backup/"
   :gens-stored-on-source-system 1
   :elements [{:type :file-compressed
               :name "user-home"
               :root-dir (str "/home/" user-name)
               :subdir-to-save ".ssh .mozilla"}]
   :backup-user {:name "dataBackupSource"
                 :encrypted-passwd "WIwn6jIUt2Rbc"}})

(s/defn ^:always-validate meissa-convention :- InfraResult
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainConfig]
  (let [{:keys [vm-user plattform] domain-config}]
    {infra/facility
      (merge
        {:vm-user user-name}
        (cond
          (= platform :virtualbox) {:settings #{:install-virtualbox-guest
                                                :install-libreoffice :install-open-jdk-8 :install-linus-basics :install-git}}
          (= platform :aws) {:tightvnc-server {:user-password "test"}
                             :settings #{:install-xfce-desktop :install-open-jdk-8 :install-linus-basics :install-git}}))}))
