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


(ns dda.pallet.dda-managed-vm.app-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.app :as sut]))

(def config-full
  {:type :desktop-office
   :user {:name "test-user"
          :password {:plain "xxx"}
          :email "test-user@mydomain.org"
          :ssh {:ssh-public-key {:plain "rsa-ssh kfjri5r8irohgn...test.key comment"}
                :ssh-private-key {:plain "123Test"}}
          :gpg {:gpg-public-key
                {:plain "-----BEGIN PGP PUBLIC KEY BLOCK-----
  ..."}
                :gpg-private-key
                {:plain "-----BEGIN PGP PRIVATE KEY BLOCK-----
  ..."}
                :gpg-passphrase {:plain "passphrase"}}}})

(def config-min
  {:type :remote
   :user {:name "test-user"
          :password {:plain "xxx"}}})

