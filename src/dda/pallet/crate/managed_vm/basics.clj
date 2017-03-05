; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.crate.managed-vm.basics
  (:require
    [pallet.actions :as actions]
    [pallet.crate.git :as git]
    [pallet.stevedore :as stevedore]
    [pallet.crate.git :as git]
    [org.domaindrivenarchitecture.pallet.crate.util :as util]
    [org.domaindrivenarchitecture.pallet.crate.package :as dda-package]
    ))



(defn install-virtualbox-guest-additions
  "make virtual machine run properly sized on virtualbox"
  []
  (actions/package "xserver-xorg-core")
  (actions/package "virtualbox-guest-dkms")
  (actions/package "virtualbox-guest-x11"))

(defn configure-virtualbox-guest-additions
  "configures virtual-box guest additions"
  [config]
  (let [os-user-name (name (-> config :vm-user))]
    (actions/exec-script ("usermod" "-G vboxsf" (str "-a " ~os-user-name)))
    ))

(defn install-xfce-desktop 
  "Install the xubuntu desktop."
  []
  (actions/package "xfce4")
  (actions/package "xfce4-goodies"))

(defn install-linus-basics 
  "Install tools for linus."
  []
  (actions/package "bash-completion")
  (actions/package "lsof")
  (actions/package "strace"))

(defn install-git
  "installs the git package"
  []
  (git/install))