; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.crate.managed-vm.convenience
  (:require
    [pallet.actions :as actions]
    ))

(defn install-user-bookmarks
  ""
  [user download-url]
  (actions/remote-file 
    (str "/home/" user "/bookmarks.html.zip") 
    :literal true
    :owner user
    :group "users"
    :force true
    :url download-url
))