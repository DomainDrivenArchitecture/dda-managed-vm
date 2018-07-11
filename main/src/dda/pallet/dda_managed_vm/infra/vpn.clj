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
(ns dda.pallet.dda-managed-vm.infra.vpn
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  (hash-set :install-openvpn
            :install-openconnect
            :install-vpnc))

(defn install-openvpn
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-openvpn")))
  (actions/packages :aptitude ["openvpn" "network-manager-openvpn"
                               "network-manager-openvpn-gnome"]))

(defn install-openconnect
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-openconnect")))
  (actions/packages :aptitude ["openconnect" "network-manager-openconnect"
                               "network-manager-openconnect-gnome"]))

(defn install-vpnc
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-vpnc")))
  (actions/packages :aptitude ["vpnc" "network-manager-vpnc"
                               "network-manager-vpnc-gnome" "vpnc-scripts"]))

(s/defn install-system
  "install common used packages for vm"
  [facility settings]
  (when (contains? settings :install-openvpn)
    (install-openvpn facility))
  (when (contains? settings :install-openconnect)
    (install-openconnect facility))
  (when (contains? settings :install-vpnc)
    (install-vpnc facility)))
