; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.infra.office
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]))

(defn install-libreoffice
  []
  (actions/package "hyphen-de")
  (actions/package "hunspell-de-de")
  (actions/package "libreoffice"))
