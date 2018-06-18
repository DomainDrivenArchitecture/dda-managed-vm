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
(ns dda.pallet.dda-managed-vm.infra.basics
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  "The basic settings"
  (hash-set (s/enum :install-virtualbox-guest :remove-power-management
                    :install-xfce-desktop  :install-os-analysis
                    :install-keymgm :configure-no-swappiness)))

(defn install-virtualbox-guest-additions
  "make virtual machine run properly sized on virtualbox"
  []
  (actions/packages :aptitude ["virtualbox-guest-x11"]))

(defn install-remove-power-management
  "remove power management on vms"
  []
  (actions/package :action :remove "xfce4-power-manager")
  (actions/package :action :remove "xfce4-power-manager-plugins")
  (actions/package :action :remove "xfce4-power-manager-data"))

(defn configure-virtualbox-guest-additions
  "configures virtual-box guest additions"
  [config]
  (let [os-user-name (name (-> config :vm-user))]
    (actions/exec-script ("usermod" "-G vboxsf" "-a" ~os-user-name))))

(defn install-xfce-desktop
  "Install the xubuntu desktop."
  []
  (actions/package "xfce4")
  (actions/package "xfce4-goodies"))

(defn install-keymgm
  "Install keymanagement tools"
  []
  (actions/package "seahorse"))

(defn install-os-analysis
  "Install analysis tools"
  []
  (actions/packages
    :aptitude ["bash-completion" "lsof" "strace"
               "htop" "iotop" "iftop"]))

(defn configure-swappiness
  []
  (actions/exec-checked-script
    "set swappiness to 0"
    ("echo \"vm.swappiness=0\"" ">>" "/etc/sysctl.conf")
    ("sysctl" "-p")))
