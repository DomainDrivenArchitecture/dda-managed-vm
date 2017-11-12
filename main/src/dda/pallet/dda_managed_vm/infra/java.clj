; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.infra.java
  (:require
    [pallet.actions :as actions]))


(defn install-open-jdk-7 []
  (actions/package "openjdk-7-jdk"))

(defn install-open-jdk-8 []
  (actions/package "openjdk-8-jdk"))

(defn install-open-jdk-9 []
  (actions/package "openjdk-9-jdk"))
