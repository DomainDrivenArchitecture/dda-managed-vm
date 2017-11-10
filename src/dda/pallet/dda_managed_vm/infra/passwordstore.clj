; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.infra.passwordstore
  (:require
    [schema.core :as s]
    [pallet.actions :as actions]
    [pallet.stevedore :as stevedore]
    [dda.config.commons.user-env :as user-env]))

(defn install-password-store
  []
  (actions/package "pass")
  (actions/package "gnupg2"))

(defn configure-password-store
  [user-name]
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/remote-file
     (str user-home "/.demo-pass")
     :owner user-name
     :group user-name
     :link (str user-home "/repo/password-store/password-store-for-teams"))
    (actions/remote-file
     (str user-home "/.bashrc.d/team-pass.sh")
     :literal true
     :content "# Load the custom .*-pass I have
for i in ~/.*-pass; do
  [ -e $i/.load.bash ] && . $i/.load.bash
done
"
     :mode "644"
     :owner user-name
     :group user-name)))
