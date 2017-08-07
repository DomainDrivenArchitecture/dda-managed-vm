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
  {:vm-user :test
   :platform :aws})

(def config-2
  {:vm-user :test
   :user-email "test@test.domain"
   :platform :aws})

(deftest test-git-config
  (testing
    "test the git config creation"
    (is (thrown? Exception (sut/vm-git-config {})))
    (is (sut/vm-git-config config-1))
    (is (sut/vm-git-config config-2))))
