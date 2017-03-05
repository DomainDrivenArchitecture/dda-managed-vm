; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.crate.managed-vm.office
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    [org.domaindrivenarchitecture.pallet.crate.util :as util]
    [org.domaindrivenarchitecture.pallet.crate.package :as dda-package]
    ))

(defn install-libreoffice 
  []
  (actions/package "hyphen-de")
  (actions/package "hunspell-de-de")
  (actions/package "libreoffice")
  )

; Install keepassx on ubuntu 14.04
;
; sudo add-apt-repository ppa:eugenesan/ppa
; apt-get update
; apt-get install keepassx
; https://chrome.google.com/webstore/detail/chromeipass/ompiailgknfdndiefoaoiligalphfdae?hl=en-US
; https://addons.mozilla.org/DE/firefox/addon/keefox/?src=search or 
; https://addons.mozilla.org/DE/firefox/addon/passifox/?src=search
;
; open: do a security risk & trust check
; Fossa reviews very old keepass but not keepassx
; https://joinup.ec.europa.eu/community/eu-fossa/og_page/code-review-log