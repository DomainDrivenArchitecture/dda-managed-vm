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


(ns dda.pallet.dda-managed-vm.domain.git-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.domain.git :as sut]))

(def min-config
  {:infra-out
   {:test-user
    {:user-email "test-user@mydomain",
     :repo
     {:books
      [{:host "github.com",
        :orga-path "DomainDrivenArchitecture",
        :repo-name "ddaArchitecture",
        :protocol :https,
        :server-type :github}]},
     :synced-repo
     {:credential-store
      [{:host "github.com",
        :orga-path "DomainDrivenArchitecture",
        :repo-name "password-store-for-teams",
        :protocol :https,
        :server-type :github}]}}}})

(deftest test-min-config
  (testing
    (is (= (:infra-out min-config)
           (sut/vm-git-config "test-user" nil nil nil nil nil)))))

(def github-ssh-config
  {:git-credentials-in {:user-name "git-test-user"
                        :host "github.com"
                        :protocol :ssh}
   :infra-out
   {:test-user
    {:user-email "test-user@mydomain",
     :credential
     {:user-name "git-test-user",
      :host "github.com",
      :protocol :ssh}
     :repo
     {:books
      [{:host "github.com",
        :orga-path "DomainDrivenArchitecture",
        :repo-name "ddaArchitecture",
        :protocol :ssh,
        :server-type :github}]},
     :synced-repo
     {:credential-store
      [{:host "github.com",
        :orga-path "DomainDrivenArchitecture",
        :repo-name "password-store-for-teams",
        :protocol :ssh,
        :server-type :github}]}}}})

(deftest test-github-ssh-config
  (testing
    (is (= (:infra-out github-ssh-config)
           (sut/vm-git-config
             "test-user" nil
             (:git-credentials-in github-ssh-config) nil nil nil)))))


(def github-ssh-with-credential-store-config
  {:git-credentials-in [{:user-name "git-test-user"
                         :host "github.com"
                         :protocol :ssh}]
   :credential-store-in [{:host "github.com",
                          :orga-path "DomainDrivenArchitecture",
                          :repo-name "additional-password-store",
                          :protocol :https,
                          :server-type :github}]
   :infra-out
   {:test-user
    {:user-email "test-user@mydomain",
     :credential
     [{:user-name "git-test-user",
       :host "github.com",
       :protocol :ssh}]
     :repo
     {:books
      [{:host "github.com",
        :orga-path "DomainDrivenArchitecture",
        :repo-name "ddaArchitecture",
        :protocol :ssh,
        :server-type :github}]},
     :synced-repo
     {:credential-store
      [{:host "github.com"
        :orga-path "DomainDrivenArchitecture"
        :repo-name "additional-password-store"
        :protocol :https
        :server-type :github}]
      :desktop-wiki []}}}})

(deftest test-github-ssh-config
  (testing
    (is (= (:infra-out github-ssh-with-credential-store-config)
           (sut/vm-git-config
             "test-user" nil
             (:git-credentials-in github-ssh-with-credential-store-config)
             nil
             []
             (:credential-store-in github-ssh-with-credential-store-config))))))

(def git-credentials-1
  [{:user-name "git-test-user",
    :host "github.com",
    :protocol :ssh}])

(def git-credentials-2
  [{:user-name "git-test-user",
    :host "gitlab.com",
    :protocol :ssh}])

(deftest test-protocol-type
  (testing
    (is (= (sut/github-protocol-type git-credentials-1) :ssh))
    (is (= (sut/github-protocol-type git-credentials-2) :https))))
