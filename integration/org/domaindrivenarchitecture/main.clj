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
(ns main
  (:require 
    [clojure.inspector :as inspector]
    [pallet.api :as api]      
    [pallet.compute :as compute]
    [pallet.compute.node-list :as node-list]
    [org.domaindrivenarchitecture.pallet.commons.session-tools :as session-tools]
    [org.domaindrivenarchitecture.pallet.commons.pallet-schema :as ps]
    [org.domaindrivenarchitecture.cm.operation :as operation]
    [org.domaindrivenarchitecture.cm.group :as group]
    [org.domaindrivenarchitecture.cm.cli-helper :as cli-helper])
  (:gen-class :main true))
  
(def localhost-node
  (node-list/make-localhost-node 
    :group-name "managed-vm-group" 
    :id :meissa-vm))

(def remote-node
  (node-list/make-node 
    "meissa-vm" 
    "managed-vm-group" 
    "10.0.2.11"
    :ubuntu
    :id :meissa-vm))

(def user (System/getenv "LOGNAME"))

(def provider
  (compute/instantiate-provider
    "node-list" :node-list [localhost-node]))

(defn install []
  (operation/do-apply-install provider (group/managed-vm-group user)))
  
(defn configure []
  (operation/do-apply-configure provider (group/managed-vm-group user)))

(defn vm-test [] 
  (operation/do-vm-test provider (group/managed-vm-group user)))

(defn -main
  "CLI main"
  [& args]
  (apply cli-helper/main install configure vm-test args))