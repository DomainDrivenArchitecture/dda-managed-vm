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
(ns dda.pallet.dda-managed-vm.domain
  (:require
    [clojure.set :as set]
    [schema.core :as s]
    [dda.config.commons.map-utils :as mu]
    [dda.config.commons.user-home :as user-home]
    [dda.pallet.commons.secret :as secret]
    [dda.pallet.dda-managed-vm.domain.user :as user]
    [dda.pallet.dda-managed-vm.domain.git :as git]
    [dda.pallet.dda-managed-vm.domain.serverspec :as serverspec]
    [dda.pallet.dda-managed-vm.domain.bookmark :as bookmark]
    [dda.pallet.dda-managed-vm.infra :as infra]))

(def DdaVmUser
  {:user {:name s/Str
          :password secret/Secret
          (s/optional-key :email) s/Str
          (s/optional-key :git-credentials) git/GitCredentials
          (s/optional-key :git-signing-key) s/Str
          (s/optional-key :ssh) {:ssh-public-key secret/Secret
                                 :ssh-private-key secret/Secret}
          (s/optional-key :gpg) {:gpg-public-key secret/Secret
                                 :gpg-private-key secret/Secret
                                 :gpg-passphrase secret/Secret}
          (s/optional-key :desktop-wiki) git/Repositories
          (s/optional-key :credential-store) git/Repositories}})

(def DdaVmUserResolved
  (secret/create-resolved-schema DdaVmUser))

(def Bookmarks infra/Bookmarks)

(def DdaVmDomainBookmarks
  {(s/optional-key :bookmarks) infra/Bookmarks})

(def DdaVmTargetType
  {:target-type (s/enum :virtualbox :remote-aws :plain)})

(def DdaVmDomainConfig
  "The convention configuration for managed vms crate."
  (merge
    DdaVmUser
    DdaVmDomainBookmarks
    DdaVmTargetType
    {:usage-type (s/enum :desktop-minimal :desktop-ide :desktop-base :desktop-office)}))

(def DdaVmDomainResolvedConfig
  "The convention configuration for managed vms crate."
  (secret/create-resolved-schema DdaVmDomainConfig))

(def InfraResult {infra/facility infra/DdaVmConfig})

(s/defn ^:always-validate user-config
  [domain-config :- DdaVmDomainResolvedConfig]
  (let [{:keys [user]} domain-config]
    {(keyword (:name user))
     (merge
       {:clear-password (:password user)}
       (user/authorized-keys user)
       (user/ssh-personal-key user)
       (user/gpg user))}))

(s/defn ^:always-validate vm-git-config
 "Git repos for VM"
 [domain-config :- DdaVmDomainResolvedConfig]
 (let [{:keys [user]} domain-config
       {:keys [name email git-credentials git-signing-key desktop-wiki credential-store]} user]
   (git/vm-git-config name email git-credentials git-signing-key desktop-wiki credential-store)))

(s/defn ^:always-validate vm-serverspec-config
 "serverspec for VM"
 [domain-config :- DdaVmDomainResolvedConfig]
 (serverspec/serverspec-prerequisits))


(s/defn ^:always-validate vm-backup-config
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainResolvedConfig]
  (let [{:keys [user]} domain-config
        {:keys [name]} user
        user-home (user-home/user-home-dir name)]
    {:backup-name "dda-managed-vm"
     :script-path "/usr/local/lib/dda-backup/"
     :gens-stored-on-source-system 1
     :elements [{:type :file-compressed
                 :name "user-home"
                 :backup-path [(str user-home ".ssh")
                               (str user-home ".gnupg")
                               (str user-home ".mozilla")
                               (str user-home ".thunderbird")]}]
     :backup-user {:name "dataBackupSource"
                   :encrypted-passwd "WIwn6jIUt2Rbc"}}))

(def desktop-minimal-settings
  #{:install-os-analysis :install-bash-utils :remove-xubuntu-unused :remove-ubuntu-unused})

(def desktop-base-settings
  (set/union
    desktop-minimal-settings
    #{:install-open-jdk-11 :install-git :install-zip-utils :install-timesync
      :install-openvpn :install-openconnect :install-vpnc :install-lightning}))

(def desktop-office-settings
  (set/union
    desktop-base-settings
    #{:install-libreoffice
      :install-spellchecking-de
      :install-diagram
      :install-keymgm
      :install-chromium
      :install-inkscape
      :install-telegram
      :install-remina
      :install-enigmail
      :install-redshift
      :install-pdf-chain}))

(def desktop-ide-settings
  (set/union
    desktop-office-settings
    #{:install-open-jdk-8}))


(s/defn ^:always-validate infra-configuration :- InfraResult
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainResolvedConfig]
  (let [{:keys [user target-type usage-type]} domain-config
        {:keys [name]} user]
    {infra/facility
      (mu/deep-merge
        {:vm-user (keyword name)
         :bookmarks (bookmark/bookmarks domain-config)}
        (when (contains? user :desktop-wiki)
          {:settings #{:install-desktop-wiki :install-diagram}})
        (when (contains? user :credential-store)
          {:credential-store (:credential-store user)
           :settings #{:install-gopass}})
        (cond
          (= usage-type :desktop-minimal)
          {:settings desktop-minimal-settings}
          (= usage-type :desktop-base)
          {:settings desktop-base-settings}
          (= usage-type :desktop-ide)
          {:settings desktop-ide-settings}
          (= usage-type :desktop-office)
          {:fakturama {:app-download-url "https://bitbucket.org/fakturamadev/fakturama-2/downloads/Fakturama_linux_x64_2.0.3.deb"
                       :doc-download-url "https://files.fakturama.info/release/v2.0.3/Handbuch-Fakturama_2.0.3.pdf"}
           :settings (set/union
                       desktop-office-settings
                       #{:install-audio})})
        (cond
          (= target-type :virtualbox)
          {:settings
            #{:install-virtualbox-guest
              :remove-power-management
              :configure-no-swappiness}}
          (= target-type :remote-aws)
          {:tightvnc-server {:user-password "test"}
           :settings
           #{:install-xfce-desktop :configure-no-swappiness}}
          (= target-type :plain)
          {:settings
           #{}}))}))
