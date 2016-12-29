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
(ns org.domaindrivenarchitecture.pallet.crate.managed-ide.instantiate-existing
  (:require
    [clojure.inspector :as inspector]
    [pallet.api :as api]      
    [pallet.compute :as compute]
    [pallet.compute.node-list :as node-list]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [org.domaindrivenarchitecture.cm.group :as group]
    [org.domaindrivenarchitecture.cm.operation :as operation]))

(def remote-node
  (node-list/make-node 
    "mmanaged-vm" 
    "managed-vm-group" 
    "35.156.245.56"
    :ubuntu
    :id :meissa-vm))

(def provider
  (compute/instantiate-provider
    "node-list"
    :node-list [remote-node]))

(defn install
  ([]
    (operation/do-apply-install provider (group/managed-vm-group "ubuntu")))
  )

(defn vm-test
  ([] 
    (operation/do-vm-test provider (group/managed-vm-group "ubuntu")))
  )
