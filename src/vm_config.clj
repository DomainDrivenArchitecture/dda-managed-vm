; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns vm-config
  (:require
    [clojure.java.io :as io]
    [org.domaindrivenarchitecture.pallet.crate.config.node :as node-record]
    [org.domaindrivenarchitecture.pallet.crate.user.ssh-key :as ssh-key-record]
    [org.domaindrivenarchitecture.pallet.crate.managed-vm :as managed-vm]
    ))

(defn dda-read-file 
  "reads a file if it exists"
  [file-name]
  (if (.exists (io/file file-name))
    (slurp file-name)
    nil))

(def ssh-keys
  {:my-key
   (ssh-key-record/new-ssh-key
     (dda-read-file (str (System/getenv "HOME") "/.ssh/id_rsa.pub"))
     (dda-read-file (str (System/getenv "HOME") "/.ssh/id_rsa")))
   })

(def os-user
  {:root   {:authorized-keys [:my-key]}
   :pallet {:authorized-keys [:my-key]}
   :vmuser {:encrypted-password "TMctxnmttcODk" ; pw=test
            :authorized-keys [:my-key]
            :personal-key :my-key}
   })

(def meissa-vm
  (node-record/new-node 
    :host-name "my-ide" 
    :domain-name "meissa-gmbh.de" 
    :pallet-cm-user-name "initial"
    :pallet-cm-user-password "test1234"
    :additional-config 
    {:dda-managed-vm {:ide-user :vmuser}
     :dda-backup managed-vm/default-vm-backup-config})
  )

(def config
  {:ssh-keys ssh-keys
   :os-user os-user
   :node-specific-config {:meissa-vm meissa-vm}
   })
