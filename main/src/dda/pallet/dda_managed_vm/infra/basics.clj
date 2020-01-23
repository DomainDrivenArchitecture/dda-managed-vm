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
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  "The basic settings"
  (hash-set
            :install-os-analysis
            :install-keymgm
            :install-xfce-desktop
            :install-virtualbox-guest
            :install-bash-utils
            :install-zip-utils
            :install-timesync
            :remove-power-management
            :remove-xubuntu-unused
            :remove-ubuntu-unused
            :configure-no-swappiness))

(defn install-virtualbox-guest-additions
  "make virtual machine run properly sized on virtualbox"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: virtualbox-guest")))
  (actions/packages :aptitude ["virtualbox-guest-x11"]))

(defn install-virtualbox-guest-additions-hwe
  "make virtual machine run properly sized on virtualbox"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: virtualbox-guest")))
  (actions/packages :aptitude ["virtualbox-guest-x11-hwe"]))

(defn configure-virtualbox-guest-additions
  "configures virtual-box guest additions"
  [facility vm-user]
  (let [os-user-name (name vm-user)]
    (actions/as-action
      (logging/info (str facility "-configure system: configure-virtualbox-guest-additions")))
    (actions/exec-script ("usermod" "-G vboxsf" "-a" ~os-user-name))))

(defn install-xfce-desktop
  "Install the xubuntu desktop."
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: xfce")))
  (actions/package "xfce4")
  (actions/package "xfce4-goodies"))

(defn install-keymgm
  "Install keymanagement tools"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: key management tools")))
  (actions/package "seahorse"))

(defn install-bash-utils
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-bash-utils")))
  (actions/packages
    :aptitude ["bash-completion" "screen"]))

(defn install-os-analysis
  "Install analysis tools"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-os-analysis")))
  (actions/packages
    :aptitude ["lsof" "strace" "ncdu" "iptraf" "htop" "iotop" "iftop"]))

(defn install-zip-utils
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-zip-utils")))
  (actions/packages
    :aptitude ["p7zip-rar" "p7zip-full" "unace" "unrar" "zip" "unzip"
               "sharutils" "rar" "mpack" "arj" "cabextract" "file-roller"]))

(defn install-timesync
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-timesync")))
  (actions/packages
    :aptitude ["timesyncd"]))

(defn configure-no-swappiness
  [facility]
  (actions/as-action
    (logging/info (str facility "-configure system: configure-no-swapiness")))
  (actions/exec-checked-script
    "set swappiness to 0"
    ("echo" "\"vm.swappiness=0\"" ">>" "/etc/sysctl.conf")
    ("sysctl" "-p")))

(defn remove-power-management
  "remove power management on vms"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: remove-power-management")))
  (actions/package "xfce4-power-manager" :action :remove)
  (actions/package "xfce4-power-manager-plugins" :action :remove)
  (actions/package "xfce4-power-manager-data" :action :remove))

(defn remove-xubuntu-unused
  "remove unused packages"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: remove-xubuntu-unused")))
  (actions/package "abiword" :action :remove)
  (actions/package "gnumeric" :action :remove))

(defn remove-ubuntu-unused
  "remove unused packages"
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: remove-ubuntu-unused")))
  (actions/package "popularity-contest" :action :remove))

(s/defn install-system
  [facility settings]
  (when (contains? settings :install-os-analysis)
    (install-os-analysis facility))
  (when (contains? settings :install-bash-utils)
    (install-bash-utils facility))
  (when (contains? settings :install-zip-utils)
    (install-zip-utils facility))
  (when (contains? settings :install-keymgm)
    (install-keymgm facility))
  (when (contains? settings :install-xfce-desktop)
    (install-xfce-desktop facility))
  (when (contains? settings :install-virtualbox-guest)
    (install-virtualbox-guest-additions-hwe facility))
  (when (contains? settings :remove-power-management)
    (remove-power-management facility))
  (when (contains? settings :remove-xubuntu-unused)
    (remove-xubuntu-unused facility))
  (when (contains? settings :remove-ubuntu-unused)
    (remove-ubuntu-unused facility)))

(s/defn configure-system
  [facility settings]
  (when (contains? settings :configure-no-swappiness)
    (configure-no-swappiness facility)))

(s/defn configure-user
  [facility user-name settings]
  (when (contains? settings :install-virtualbox-guest)
    (configure-virtualbox-guest-additions facility user-name)))
