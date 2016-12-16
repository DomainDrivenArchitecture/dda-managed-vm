; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns main
  (:require 
    [org.domaindrivenarchitecture.pallet.core.cli-helper :as cli-helper]
    [org.domaindrivenarchitecture.pallet.crate.config :as config]
    [org.domaindrivenarchitecture.pallet.crate.init :as init]
    [org.domaindrivenarchitecture.pallet.crate.backup :as backup]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm :as managed-vm]
    [vm-config]
    [pallet.api :as api]
    [pallet.compute :as compute]
    [pallet.compute.node-list :as node-list])
  (:gen-class :main true))
  
(def managed-vm-group
  (api/group-spec
    "managed-vm-group"
    :extends [(config/with-config vm-config/config) 
              init/with-init
              backup/with-backup
              managed-vm/with-dda-vm]))

(def localhost-node
  (node-list/make-localhost-node 
    :group-name "managed-vm-group" 
    :id :meissa-vm))

(def remote-node
  (node-list/make-node 
    "meissa-vm" 
    "managed-vm-group" 
    "10.0.2.11"
    :ubuntu
    :id :meissa-vm))

(def node-list
  (compute/instantiate-provider
    "node-list" :node-list [localhost-node]))

(defn -main
  "CLI main"
  [& args]
  (apply cli-helper/main 
         :meissa-vm
         managed-vm-group
         node-list 
         vm-config/config
         args))