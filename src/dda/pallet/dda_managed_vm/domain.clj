; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.domain
  (:require
    [schema.core :as s]
    [dda.config.commons.map-utils :as mu]
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

(s/defn ^:always-validate vm-git-config :- git/GitDomainConfig
 "Git repos for VM"
 [vm-config :- DdaVmDomainConfig]
 (let [{:keys [vm-user user-email]} vm-config
       used-email (if (contains? vm-config :user-email)
                      user-email
                      (str (name vm-user) "@domain"))]
   {:os-user vm-user
    :user-email used-email
    :repos {:dda-book
            ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"
             "https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}}))

(s/defn ^:always-validate vm-serverspec-config :- serverspec/ServerTestDomainConfig
 "serverspec for VM"
 [vm-config :- DdaVmDomainConfig]
 (let [{:keys [platform vm-user]} vm-config]
   (cond
     (= platform :aws)
     {:package {:xfce4 {:installed? true}}
      :netstat {:Xtightvnc {:port "5901"}}}
     :default {})))

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

(s/defn ^:always-validate infra-configuration :- InfraResult
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainConfig]
  (let [{:keys [vm-user platform]} domain-config]
    {infra/facility
      (merge
        {:vm-user vm-user
         :bookmarks [{:name "Bookmarks Toolbar"
                      :links [["https://domaindrivenarchitecture.org/" "dda"]]
                      :childs [{:name "WebConf"
                                :links [["https://meet.jit.si/dda-pallet" "jitsi dda-pallet"]
                                        ["http://meetingwords.com/" "MeetingWords"]
                                        ["https://web.telegram.org/" "Telegram"]
                                        ["http://www.meebl.de/" "meebl"]]}]}]}
        (cond
          (= platform :virtualbox)
          {:settings
            #{:install-virtualbox-guest
              :install-libreoffice :install-open-jdk-8
              :install-analysis :install-git :install-password-store}}
          (= platform :aws)
          {:tightvnc-server {:user-password "test"
                             :settings
                             #{:install-xfce-desktop :install-open-jdk-8
                               :install-analysis :install-git}}}))}))
