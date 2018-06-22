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
  {:user {:name  "test"
          :password "pwd"}
   :target-type :remote-aws
   :usage-type :desktop-base})

(def config-2
  {:user {:name  "test"
          :password "pwd"}
   :credentials ["credentials-repo"]
   :desktop-wiki ["wiki-autosync-repo"]
   :target-type :virtualbox
   :usage-type :desktop-office})

(def config-3
  {:target-type :virtualbox
   :usage-type :desktop-minimal
   :bookmarks [{:name "Bookmarks Toolbar"
                :links [["url" "name"]]}]
   :user {:name  "test"
          :password "pwd"}})

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
            :credentials ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"]}
           :synced-repos {}}
         (sut/vm-git-config config-1)))
    (is (=
          {:os-user :test,
           :user-email "test@mydomain",
           :repos
           {:book ["https://github.com/DomainDrivenArchitecture/ddaArchitecture.git"],
            :credentials ["https://github.com/DomainDrivenArchitecture/password-store-for-teams.git"
                          "credentials-repo",]}
           :synced-repos {:wiki ["wiki-autosync-repo"]}}
          (sut/vm-git-config config-2)))))

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
            :settings #{:install-os-analysis :install-git
                        :install-open-jdk-11 :configure-no-swappiness
                        :install-xfce-desktop,}
            :tightvnc-server {:user-password "test"}}}
          (sut/infra-configuration config-1)))
    (is (=
          {:dda-managed-vm
            {:vm-user :test,
             :bookmarks [{:name "Bookmarks Toolbar", :links [["https://domaindrivenarchitecture.org/" "dda"]], :childs [{:name "WebConf", :links [["https://meet.jit.si/dda-pallet" "jitsi dda-pallet"] ["http://meetingwords.com/" "MeetingWords"] ["https://web.telegram.org/" "Telegram"] ["http://www.meebl.de/" "meebl"]]}]}],
                    :settings #{:install-os-analysis :install-chromium :install-keymgm :install-git :install-open-jdk-11
                                :install-spellchecking-de :install-telegram :configure-no-swappiness :install-inkscape
                                :install-desktop-wiki :install-libreoffice :remove-power-management :install-pdf-chain
                                :install-gopass :install-virtualbox-guest},
                    :fakturama {:app-download-url "https://files.fakturama.info/release/v2.0.2/Fakturama_linux_x64_2.0.2.1.deb",
                                :doc-download-url "https://files.fakturama.info/release/v2.0.2/Handbuch-Fakturama_2.0.2.pdf"}}}
          (sut/infra-configuration config-2)))))
