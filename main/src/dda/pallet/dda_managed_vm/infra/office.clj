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
(ns dda.pallet.dda-managed-vm.infra.office
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [selmer.parser :as selmer]
    [pallet.actions :as actions]
    [dda.config.commons.user-home :as user-env]))

(def FakturamaConfig
  "The configuration for managed vms crate."
  {:app-download-url s/Str
   :doc-download-url s/Str})

(def Settings
  (hash-set :install-libreoffice
            :install-spellchecking-de
            :install-inkscape
            :install-pdf-chain
            :install-audio
            :install-redshift))

(defn install-libreoffice
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: libreoffice")))
  (actions/package "libreoffice"))

(defn install-spellchecking-de
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: spellchecking-de")))
  (actions/packages :aptitude ["hyphen-de" "hunspell" "hunspell-de-de"]))

(defn install-inkscape
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: inkscape")))
  (actions/package "inkscape"))

; only gprename and a2ps are existant on ubuntu 18.04
(defn install-pdf-chain
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: pdf-chain")))
  ;(actions/packages :aptitude ["pdfchain" "pdftk" "gprename" "pyrenamer" "a2ps"])
  (actions/exec-script ("snap" "install" "pdftk")))

(defn install-audio
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-audio")))
  (actions/packages :aptitude ["ubuntu-restricted-extras" "uudeview"
                               "xine-ui" "icedax" "easytag" "id3tool"
                               "lame" "nautilus-script-audio-convert" "libmad0"
                               "mpg321" "libavcodec-extra" "libdvd-pkg"]))

(s/defn install-fakturama
  "get and install fakturama"
  [facility
   fakturama-config :- FakturamaConfig]
  (actions/as-action
    (logging/info (str facility "-install system: fakturama")))
  (actions/remote-file
    "/tmp/installer_fakturama_linux_64Bit.deb"
    :url (get-in fakturama-config [:app-download-url]))
  (actions/exec-checked-script
      "install fakturama"
      ("dpkg" "-i" "/tmp/installer_fakturama_linux_64Bit.deb"))
  (actions/directory
    "/opt/fakturama-doc"
    :owner "root"
    :group "users"
    :mode "755")
  (actions/remote-file
    "/opt/fakturama-doc/handbuch.pdf"
    :literal true
    :owner "root"
    :group "users"
    :mode "755"
    :url (get-in fakturama-config [:doc-download-url])))

(defn install-redshift
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-redshift")))
  (actions/packages :aptitude ["redshift" "redshift-gtk"])
  (actions/remote-file
    "/etc/geoclue/geoclue.conf"
    :literal true
    :owner "root"
    :group "root"
    :mode "644"
    :content (selmer/render-file "geoclue.conf.templ" {})))

(s/defn configure-user-redshift
  [facility :- s/Keyword
   user-name :- s/Str]
  (actions/as-action
   (logging/info (str facility "-configure user: configure-user-redshift")))
  (let [user-home (user-env/user-home-dir user-name)]
    (actions/directory
      (str user-home "/.config")
      :owner user-name
      :group user-name
      :mode "755")
    (actions/remote-file
      (str user-home "/.config/redshift.conf")
      :literal true
      :owner user-name
      :group user-name
      :mode "644"
      :content (selmer/render-file "redshift.conf.templ" {}))))

(s/defn install-system
  "install common used packages for vm"
  [facility config]
  (let [{:keys [settings fakturama]} config]
    (when (contains? settings :install-spellchecking-de)
      (install-spellchecking-de facility))
    (when (contains? settings :install-libreoffice)
      (install-libreoffice facility))
    (when (contains? settings :install-inkscape)
      (install-inkscape facility))
    (when (contains? settings :install-pdf-chain)
      (install-pdf-chain facility))
    (when (contains? settings :install-audio)
      (install-audio facility))
    (when (contains? config :fakturama)
      (install-fakturama facility (get-in config [:fakturama])))
    (when (contains? config :install-redshift)
      (install-redshift facility))))

(s/defn configure-user
  [facility :- s/Keyword
   user-name :- s/Str
   config]
  (when (contains? config :install-redshift)
    (configure-user-redshift facility user-name)))
