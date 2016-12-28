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

(ns org.domaindrivenarchitecture.pallet.crate.managed-vm.test-vm
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.api :as api]
    [pallet.actions :as actions]
    [pallet.crate :as crate]
    [pallet.phase :as phase]
    [org.domaindrivenarchitecture.pallet.core.dda-crate :as dda-crate]
    [org.domaindrivenarchitecture.pallet.servertest.fact.packages :as package-fact]
    [org.domaindrivenarchitecture.pallet.servertest.test.packages :as package-test]
    [org.domaindrivenarchitecture.pallet.servertest.fact.netstat :as netstat-fact]
    [org.domaindrivenarchitecture.pallet.servertest.test.netstat :as netstat-test]))

(defn collect-facts [config]
  (netstat-fact/collect-netstat-fact)
  (package-fact/collect-packages-fact)
  )

(defn test-vm [config]
  (netstat-test/test-prog-listen "Xtightvnc" 5901)
  (package-test/test-installed? "xfce4")
  )
