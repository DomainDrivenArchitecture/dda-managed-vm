; Licensed to the Apache Software Foundation (ASF) under one
; or more contributor license agreements. See the NOTICE file
; distributed with this work for additional information
; regarding copyright ownership. The ASF licenses this file
; to you under the Apache License, Version 2.0 (the
; "License"); you may not use this file except in compliance
; with the License. You may obtain a copy of the License at
;
; http://www.apache.org/licenses/LICENSE-2.0
;
; Unless required by applicable law or agreed to in writing, software
; distributed under the License is distributed on an "AS IS" BASIS,
; WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
; See the License for the specific language governing permissions and
; limitations under the License.
(ns dda.pallet.dda-managed-vm.infra.communication
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  (hash-set :install-telegram
            :install-remina
            :install-enigmail
            :install-lightning))

(defn install-telegram
  [facility]
  "get and install telegram at /opt/telegram"
  (actions/as-action
   (logging/info (str facility "-install system: install-telegram")))
  (actions/remote-directory
    "/opt/telegram"
    :owner "root"
    :group "users"
    :recursive true
    :unpack :tar
    :tar-options "x"
    :url
    "https://telegram.org/dl/desktop/linux")
  (actions/remote-file
    "/etc/profile.d/telegram.sh"
    :literal true
    :content "PATH=$PATH:/opt/telegram
export PATH"))

(defn install-enigmail
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-enigmail")))
  (actions/packages :aptitude ["enigmail"]))

(defn install-lightning
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-lightning")))
  (actions/packages :aptitude ["xul-ext-lightning"]))

(defn install-remina
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-remina")))
  (actions/packages :aptitude ["remmina"
                               "remmina-plugin-rdp"]))

(s/defn install-system
  "install common used packages for vm"
  [facility settings]
  (when (contains? settings :install-enigmail)
    (install-enigmail facility))
  (when (contains? settings :install-lightning)
    (install-lightning facility))
  (when (contains? settings :install-telegram)
    (install-telegram facility))
  (when (contains? settings :install-remina)
    (install-remina facility)))
