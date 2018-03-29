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
    [pallet.actions :as actions]))

(defn install-virtualbox-guest-additions
  "make virtual machine run properly sized on virtualbox"
  []
  (actions/exec-checked-script
   "install virtualbox guest utils"
   ("apt-get" "install -y" "xserver-xorg-core xserver-xorg-input-vmmouse"
     "xserver-xorg-input-all" "virtualbox-guest-dkms" "virtualbox-guest-x11")))

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

(defn install-analysis
  "Install analysis tools"
  []
  (actions/package "bash-completion")
  (actions/package "lsof")
  (actions/package "strace")
  (actions/package "htop")
  (actions/package "iotop")
  (actions/package "iftop"))
