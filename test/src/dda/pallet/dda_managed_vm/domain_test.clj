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
          :credentials ["credentials-repo"]
          :desktop-wiki ["wiki-autosync-repo"]}})

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
          :git-credentials {:gitblit {:user "user-gb"}
                            :github {:user "user-gh"
                                     :password "pwd-gh"}}
          :credentials ["credentials-repo"]
          :desktop-wiki ["wiki-autosync-repo"]}})

(def config-set-ide
  {:domain-input {:target-type :virtualbox
                  :usage-type :desktop-ide
                  :user {:name  "test"
                         :password "pwd"
                         :credentials ["credentials-repo"]
                         :desktop-wiki ["wiki-autosync-repo"]}}
   :git-domain {:os-user :test,
                :user-email "test@mydomain",
                :repos
                {:book
                 ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"],
                 :credentials
                 ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"
                  "credentials-repo"]},
                :synced-repos {:wiki ["wiki-autosync-repo"]}}
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
                              :install-virtualbox-guest :install-timesync,}
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

(deftest test-git-config
  (testing
    "test the git config creation"
    (is (thrown? Exception (sut/vm-git-config {})))
    (is (=
          {:os-user :test,
           :user-email "test@mydomain",
           :repos
           {:book ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"],
            :credentials ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}}
         (sut/vm-git-config config-1)))
    (is (=
          {:os-user :test,
           :user-email "test@mydomain",
           :repos
           {:book ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"],
            :credentials ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"
                          "credentials-repo",]}
           :synced-repos {:wiki ["wiki-autosync-repo"]}}
          (sut/vm-git-config config-2)))
    (is (=
          {:os-user :test,
           :user-email "test@mydomain",
           :credentials {:gitblit {:user "user-gb"},
                         :github {:user "user-gh", :password "pwd-gh"}},
           :repos
           {:book ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"],
            :credentials ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"
                          "credentials-repo",]}
           :synced-repos {:wiki ["wiki-autosync-repo"]}}
          (sut/vm-git-config config-4)))
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
              :configure-no-swappiness :install-xfce-desktop :install-audio
              :install-openvpn :install-timesync}
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
                      :remove-xubuntu-unused :install-vpnc :install-telegram :configure-no-swappiness
                      :install-inkscape :install-remina :install-audio :install-desktop-wiki
                      :install-libreoffice :install-openvpn :remove-power-management :install-gopass
                      :install-virtualbox-guest :install-timesync,}
                    :fakturama {:app-download-url "https://files.fakturama.info/release/v2.0.2/Fakturama_linux_x64_2.0.2.1.deb",
                                :doc-download-url "https://files.fakturama.info/release/v2.0.2/Handbuch-Fakturama_2.0.2.pdf"}}}
          (sut/infra-configuration config-2)))
    (is (= (:infra config-set-ide)
           (sut/infra-configuration (:domain-input config-set-ide))))))
