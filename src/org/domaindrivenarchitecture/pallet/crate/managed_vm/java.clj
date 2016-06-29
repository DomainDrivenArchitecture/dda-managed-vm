; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns org.domaindrivenarchitecture.pallet.crate.managed-vm.java
  (:require
    [pallet.actions :as actions]
    ))

(defn install-open-jdk-7 []
  (actions/package "openjdk-7-jdk")
  )