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


(ns dda.pallet.dda-managed-vm.infra-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.infra :as sut]))

(def example-configuration
  {:vm-user :test})

(def example-hashset-configuration
  {:vm-user :test
   :settings (hash-set :install-virtualbox-guest :failure)})


(def example-hashset-configuration2
  {:vm-user :test
   :settings (hash-set :install-virtualbox-guest
                       :install-libreoffice :install-open-jdk-8)})




(deftest test-schema
  (testing
    "test the config schema"
    (is (s/validate sut/DdaVmConfig example-configuration))
    (is (thrown? Exception (s/validate sut/DdaVmConfig example-hashset-configuration)))
    (is (s/validate sut/DdaVmConfig example-hashset-configuration2))))


(deftest plan-def
  (testing
    "test plan-def"
    (is sut/with-dda-vm)))
