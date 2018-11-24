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


(ns dda.pallet.dda-managed-vm.domain-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.domain :as sut]))

(def config-1
  {:target-type :remote-aws
   :usage-type :desktop-base
   :user {:name  "test"
          :password "pwd"}})

(def config-2
  {:target-type :virtualbox
   :usage-type :desktop-office
   :user {:name  "test"
          :password "pwd"
          :git-credentials [{:host "github.com"
                             :protocol :ssh
                             :user-name "test"}]}})

(def config-3
  {:target-type :virtualbox
   :usage-type :desktop-minimal
   :bookmarks [{:name "Bookmarks Toolbar"
                :links [["url" "name"]]}]
   :user {:name  "test"
          :password "pwd"}})

(def config-4
  {:target-type :virtualbox
   :usage-type :desktop-office
   :user {:name  "test"
          :password "pwd"
          :git-credentials [{:host "github.com"
                             :protocol :ssh
                             :user-name "test"}]
          :desktop-wiki [{:host "github.com"
                          :orga-path "mypath"
                          :repo-name "mywiki"
                          :protocol :ssh
                          :server-type :github}]}})

(def config-set-ide
  {:domain-input {:target-type :virtualbox
                  :usage-type :desktop-ide
                  :user {:name  "test"
                         :password "pwd"
                         :credential-store [{:host "github.com",
                                             :orga-path "DomainDrivenArchitecture",
                                             :repo-name "additional-password-store",
                                             :protocol :https,
                                             :server-type :github,}]
                         :desktop-wiki [{:host "github.com"
                                         :orga-path "mypath"
                                         :repo-name "mywiki"
                                         :protocol :ssh
                                         :server-type :github}]}}
   :git-domain {:test
                {:user-email "test@mydomain"
                 :repo {:books
                        [{:host "github.com"
                          :orga-path "DomainDrivenArchitecture"
                          :repo-name "ddaArchitecture"
                          :protocol :https
                          :server-type :github}]}
                 :synced-repo  {:credential-store
                                [{:host "github.com"
                                  :orga-path "DomainDrivenArchitecture"
                                  :repo-name "password-store-for-teams"
                                  :protocol :https
                                  :server-type :github}
                                 {:host "github.com",
                                  :orga-path "DomainDrivenArchitecture",
                                  :repo-name "additional-password-store",
                                  :protocol :https,
                                  :server-type :github,}]
                                :desktop-wiki
                                [{:host "github.com"
                                  :orga-path "mypath"
                                  :repo-name "mywiki"
                                  :protocol :ssh
                                  :server-type :github}]}}}
   :infra {:dda-managed-vm {:settings
                            #{:install-os-analysis :install-chromium
                              :install-enigmail :install-keymgm
                              :install-open-jdk-11 :install-open-jdk-8
                              :install-zip-utils :install-git :remove-ubuntu-unused
                              :install-bash-utils :install-diagram
                              :install-openconnect
                              :install-spellchecking-de :remove-xubuntu-unused
                              :install-vpnc :install-telegram
                              :configure-no-swappiness :install-inkscape
                              :install-remina :install-desktop-wiki
                              :install-libreoffice :install-openvpn
                              :remove-power-management :install-gopass
                              :install-virtualbox-guest :install-timesync,
                              :install-lightning}
                            :bookmarks
                            [{:name "Bookmarks Toolbar",
                              :links
                              [["https://domaindrivenarchitecture.org/" "dda"]],
                              :childs
                              [{:name "WebConf",
                                :links
                                [["https://meet.jit.si/dda-pallet"
                                  "jitsi dda-pallet"]
                                 ["http://meetingwords.com/" "MeetingWords"]
                                 ["https://web.telegram.org/" "Telegram"]
                                 ["http://www.meebl.de/" "meebl"]]}]}],
                            :vm-user :test}}})

(deftest test-backup-config
  (testing
    "test the git config creation"
    (is (thrown? Exception (sut/vm-backup-config {})))
    (is (sut/vm-backup-config config-1))
    (is (sut/vm-backup-config config-2))))

(def gitconfig-result1
  {:test
     {:user-email "test@mydomain"
      :repo {:books
             [{:host "github.com"
               :orga-path "DomainDrivenArchitecture"
               :repo-name "ddaArchitecture"
               :protocol :https
               :server-type :github}]}
      :synced-repo  {:credential-store
                     [{:host "github.com"
                       :orga-path "DomainDrivenArchitecture"
                       :repo-name "password-store-for-teams"
                       :protocol :https
                       :server-type :github}]}}})

(def gitconfig-result2
  {:test
    {:user-email "test@mydomain"
     :credential [{:host "github.com"
                   :protocol :ssh
                   :user-name "test"}]
     :repo {:books
            [{:host "github.com"
              :orga-path "DomainDrivenArchitecture"
              :repo-name "ddaArchitecture"
              :protocol :ssh
              :server-type :github}]}
     :synced-repo  {:credential-store
                    [{:host "github.com"
                      :orga-path "DomainDrivenArchitecture"
                      :repo-name "password-store-for-teams"
                      :protocol :ssh
                      :server-type :github}]}}})

(def gitconfig-result4
  {:test
    {:user-email "test@mydomain"
     :credential [{:host "github.com"
                   :protocol :ssh
                   :user-name "test"}]
     :repo {:books
            [{:host "github.com"
              :orga-path "DomainDrivenArchitecture"
              :repo-name "ddaArchitecture"
              :protocol :ssh
              :server-type :github}]}
     :synced-repo {:credential-store
                    [{:host "github.com"
                      :orga-path "DomainDrivenArchitecture"
                      :repo-name "password-store-for-teams"
                      :protocol :ssh
                      :server-type :github}]
                    :desktop-wiki
                    [{:host "github.com"
                      :orga-path "mypath"
                      :repo-name "mywiki"
                      :protocol :ssh
                      :server-type :github}]}}})

(deftest test-git-config
  (testing
    "test the git config creation"
    (is (thrown? Exception (sut/vm-git-config {})))
    (is (= gitconfig-result1 (sut/vm-git-config config-1)))
    (is (= gitconfig-result2 (sut/vm-git-config config-2)))
    (is (= gitconfig-result4 (sut/vm-git-config config-4)))
    (is (= (:git-domain config-set-ide)
           (sut/vm-git-config (:domain-input config-set-ide))))))

(deftest test-serverspec-config
  (testing
    "test the serverspec config creation"
    (is (thrown? Exception (sut/vm-serverspec-config {})))
    (is (sut/vm-serverspec-config config-1))
    (is (sut/vm-serverspec-config config-2))))

(deftest test-infra-configuration
  (testing
    "test the serverspec config creation"
    (is (thrown? Exception (sut/infra-configuration {})))
    (is (=
          {:dda-managed-vm
           {:vm-user :test,
            :bookmarks [{:name "Bookmarks Toolbar", :links [["https://domaindrivenarchitecture.org/" "dda"]], :childs [{:name "WebConf", :links [["https://meet.jit.si/dda-pallet" "jitsi dda-pallet"] ["http://meetingwords.com/" "MeetingWords"] ["https://web.telegram.org/" "Telegram"] ["http://www.meebl.de/" "meebl"]]}]}],
            :settings
            #{:install-os-analysis :install-zip-utils :install-git
              :remove-ubuntu-unused :install-bash-utils :install-openconnect
              :install-open-jdk-11 :remove-xubuntu-unused :install-vpnc
              :install-lightning :configure-no-swappiness :install-xfce-desktop
              :install-audio :install-openvpn :install-timesync}
            :tightvnc-server {:user-password "test"}}}
          (sut/infra-configuration config-1)))
    (is (=
          {:dda-managed-vm
            {:vm-user :test,
             :bookmarks [{:name "Bookmarks Toolbar", :links [["https://domaindrivenarchitecture.org/" "dda"]], :childs [{:name "WebConf", :links [["https://meet.jit.si/dda-pallet" "jitsi dda-pallet"] ["http://meetingwords.com/" "MeetingWords"] ["https://web.telegram.org/" "Telegram"] ["http://www.meebl.de/" "meebl"]]}]}],
                    :settings
                    #{:install-os-analysis :install-chromium :install-enigmail :install-keymgm
                      :install-zip-utils :install-git :remove-ubuntu-unused :install-bash-utils
                      :install-diagram :install-openconnect :install-open-jdk-11 :install-spellchecking-de
                      :remove-xubuntu-unused :install-vpnc :install-telegram :install-lightning
                      :configure-no-swappiness :install-inkscape :install-remina :install-audio
                      :install-libreoffice :install-openvpn :remove-power-management
                      :install-virtualbox-guest :install-timesync,}
                    :fakturama {:app-download-url "https://files.fakturama.info/release/v2.0.3/Fakturama_linux_x64_2.0.3.deb",
                                :doc-download-url "https://files.fakturama.info/release/v2.0.3/Handbuch-Fakturama_2.0.3.pdf"}}}
          (sut/infra-configuration config-2)))
    (is (= (:infra config-set-ide)
           (sut/infra-configuration (:domain-input config-set-ide))))))
