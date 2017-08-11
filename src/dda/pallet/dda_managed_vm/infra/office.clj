; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.infra.office
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    [org.domaindrivenarchitecture.pallet.crate.util :as util]
    [org.domaindrivenarchitecture.pallet.crate.package :as dda-package]))

(defn install-libreoffice
  []
  (actions/package "hyphen-de")
  (actions/package "hunspell-de-de")
  (actions/package "libreoffice"))

(defn install-password-store
  []
  (actions/package "pass")
  (actions/package "gnupg2"))
