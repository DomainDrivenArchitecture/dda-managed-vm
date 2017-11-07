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
    [schema.core :as s]
    [dda.config.commons.map-utils :as mu]
    [dda.pallet.dda-managed-vm.domain.user :as user]
    [dda.pallet.dda-managed-vm.domain.git :as git]
    [dda.pallet.dda-managed-vm.domain.bookmark :as bookmark]
    [dda.pallet.dda-managed-vm.infra :as infra]))

(def Secret {(s/optional-key :plain) s/Str
             (s/optional-key :password-store-single) s/Str
             (s/optional-key :password-store-multi) s/Str})

(def DdaVmDomainConfig
  "The convention configuration for managed vms crate."
  {:user {:name s/Str
          :password Secret
          :email s/Str
          (s/optional-key :ssh) {:ssh-public-key Secret
                                 :ssh-private-key Secret}
          (s/optional-key :gpg) {:gpg-public-key Secret
                                 :gpg-private-key Secret
                                 :gpg-passphrase Secret}}
   :type (s/enum :desktop-minimal :desktop-office :remote)
   (s/optional-key :bookmarks) infra/Bookmarks})

(def DdaVmDomainResolvedConfig
  "The convention configuration for managed vms crate."
  {:user {:name s/Str
          :password s/Str
          (s/optional-key :email) s/Str
          (s/optional-key :ssh) {:ssh-public-key s/Str
                                 :ssh-private-key s/Str}
          (s/optional-key :gpg) {:gpg-public-key s/Str
                                 :gpg-private-key s/Str
                                 :gpg-passphrase s/Str}}
   :type (s/enum :desktop-minimal :desktop-office :remote)
   (s/optional-key :bookmarks) infra/Bookmarks})

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
 (git/vm-git-config domain-config))

(s/defn ^:always-validate vm-serverspec-config
 "serverspec for VM"
 [domain-config :- DdaVmDomainResolvedConfig]
 (let [{:keys [type user]} domain-config]
   (cond
     (= type :remote)
     {:package {:xfce4 {:installed? true}}
      :netstat {:Xtightvnc {:port "5901"}}}
     :default {})))

(s/defn ^:always-validate vm-backup-config
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainResolvedConfig]
  (let [{:keys [user]} domain-config
        {:keys [name]} user]
    {:backup-name "dda-managed-vm"
     :script-path "/usr/local/lib/dda-backup/"
     :gens-stored-on-source-system 1
     :elements [{:type :file-compressed
                 :name "user-home"
                 :root-dir (str "/home/" name)
                 :subdir-to-save ".ssh .gnupg .mozilla"}]
     :backup-user {:name "dataBackupSource"
                   :encrypted-passwd "WIwn6jIUt2Rbc"}}))

(s/defn ^:always-validate infra-configuration :- InfraResult
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainResolvedConfig]
  (let [{:keys [user type]} domain-config
        {:keys [name]} user]
    {infra/facility
      (merge
        {:vm-user (keyword name)
         :bookmarks (bookmark/bookmarks domain-config)}
        (cond
          (= type :desktop-minimal)
          {:settings
            #{:install-virtualbox-guest :install-analysis}}
          (= type :desktop-office)
          {:settings
            #{:install-virtualbox-guest
              :install-libreoffice :install-open-jdk-8
              :install-analysis :install-git :install-password-store}}
          (= type :remote)
          {:tightvnc-server {:user-password "test"}
           :settings
           #{:install-xfce-desktop :install-open-jdk-8 :install-analysis
             :install-git}}))}))
