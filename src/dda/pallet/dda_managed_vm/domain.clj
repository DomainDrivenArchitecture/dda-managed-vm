; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
; http://www.apache.org/licenses/LICENSE-2.0
;
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
    [dda.pallet.dda-managed-vm.infra :as infra]))

(def DdaVmDomainConfig
  "The convention configuration for managed vms crate."
  {:user {:name s/Str
          :password s/Str
          :email s/Str
          (s/optional-key :ssh) {:ssh-public-key s/Str
                                 :ssh-private-key s/Str}
          (s/optional-key :gpg) {:gpg-public-key s/Str
                                 :gpg-private-key s/Str
                                 :gpg-passphrase s/Str}}
   :platform (s/enum :virtualbox :aws)})

(def InfraResult {infra/facility infra/DdaVmConfig})


(s/defn ^:always-validate user-config
  [domain-config :- DdaVmDomainConfig]
  (let [{:keys [user]} domain-config]
    {(keyword (:name user))
     (merge
       {:clear-password (:password user)}
       (user/authorized-keys user)
       (user/ssh-personal-key user)
       (user/gpg user))}))

(s/defn ^:always-validate vm-git-config
 "Git repos for VM"
 [domain-config :- DdaVmDomainConfig]
 (let [{:keys [user]} domain-config
       {:keys [name email]} user]
   {:os-user (keyword name)
    :user-email email
    :repos {:stuff
            ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"
             "https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}}))

(s/defn ^:always-validate vm-serverspec-config
 "serverspec for VM"
 [domain-config :- DdaVmDomainConfig]
 (let [{:keys [platform user]} domain-config]
   (cond
     (= platform :aws)
     {:package {:xfce4 {:installed? true}}
      :netstat {:Xtightvnc {:port "5901"}}}
     :default {})))

(s/defn ^:always-validate vm-backup-config
  "Managed vm crate default configuration"
  [domain-config :- DdaVmDomainConfig]
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
  [domain-config :- DdaVmDomainConfig]
  (let [{:keys [user platform]} domain-config
        {:keys [name]} user]
    {infra/facility
      (merge
        {:vm-user (keyword name)
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
          {:tightvnc-server {:user-password "test"}
           :settings
           #{:install-xfce-desktop :install-open-jdk-8 :install-analysis
             :install-git}}))}))
