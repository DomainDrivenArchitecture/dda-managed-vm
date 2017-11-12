; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

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

(defn install-analysis
  "Install analysis tools"
  []
  (actions/package "bash-completion")
  (actions/package "lsof")
  (actions/package "strace")
  (actions/package "htop")
  (actions/package "iotop")
  (actions/package "iftop"))
