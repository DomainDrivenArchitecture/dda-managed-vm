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
(ns dda.pallet.dda-managed-vm.infra.tightvnc
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]
    [dda.pallet.crate.util :as util]
    [dda.config.commons.user-home :as user-env]))

(def Tightvnc {:user-password s/Str})

(defn set-user-password [os-user password]
   (let [vnc-path (str (user-env/user-home-dir os-user) "/.vnc")]
     (actions/exec-checked-script
         "set-vnc-user-password"
         (pipe (println ~password) ("vncpasswd" -f > ~(str vnc-path "/passwd")))
         ("chown" "-R" ~(str os-user ":" os-user) ~vnc-path)
         ("chmod" "0600" ~(str vnc-path "/passwd")))))

(s/defn install-system-tightvnc-server
  "Install remote desktop viewing."
  [facility :- s/Keyword]
  (actions/as-action
   (logging/info (str facility "-install system: tightvnc")))
  (actions/package "tightvncserver"))

(s/defn install-user-tightvnc-server
  "Install remote desktop viewing."
  [facility :- s/Keyword
   user-name :- s/Str
   config :- Tightvnc]
  (let [{:keys [user-password]} config
        vnc-path (str (user-env/user-home-dir user-name) "/.vnc")
        vnv-service-name (str "vncserver@" user-name ".service")]
    (actions/as-action
     (logging/info (str facility "-install user: install-user-tightvnc-server")))
    (actions/directory vnc-path :owner user-name :group user-name)
    (actions/remote-file
      (str vnc-path "/xstartup")
      :owner user-name
      :group user-name
      :mode "0700"
      :literal true
      :content (util/create-file-content
                 ["#!/bin/bash"
                  "source /etc/profile"
                  "xrdb $HOME/.Xresources"
                  "startxfce4 &"
                  "xfconf-query -c xfce4-keyboard-shortcuts -p /xfwm4/custom/'<'Super'>'Tab -r"]))))

(s/defn install-user-vnc-tab-workaround
  "Install a small script to fix tab issue on vnc."
  [facility :- s/Keyword
   user-name :- s/Str]
  (let [script-path (str (user-env/user-home-dir user-name) "/vnc-tab-workaround.sh")]
    (actions/as-action
     (logging/info (str facility "-install user: install-user-vnc-tab-workaround")))
    (actions/remote-file
      script-path
      :owner user-name
      :group user-name
      :mode "0700"
      :literal true
      :content (util/create-file-content
                 ["#!/bin/bash"
                  "xfconf-query -c xfce4-keyboard-shortcuts -p /xfwm4/custom/'<'Super'>'Tab -r"]))))

(s/defn configure-system-tightvnc-server
  "Install remote desktop viewing."
  [facility :- s/Keyword
   user-name :- s/Str]
  (let [vnv-service-name (str "vncserver@1" ".service")]
    (actions/as-action
     (logging/info (str facility "-configure system: tightvnc")))
    (actions/remote-file
      (str "/etc/systemd/system/" vnv-service-name)
      :owner "root"
      :group "root"
      :mode "0644"
      :literal true
      :content (util/create-file-content
                 ["[Unit]"
                  "Description=Start TightVNC server at startup"
                  "After=syslog.target network.target"
                  ""
                  "[Service]"
                  "Type=forking"
                  (str "User=" user-name)
                  "PAMName=login"
                  (str "PIDFile=/home/" user-name "/.vnc/%H:%i.pid")
                  "ExecStartPre=-/usr/bin/vncserver -kill :%i > /dev/null 2>&1"
                  "ExecStart=/usr/bin/vncserver -depth 24 -geometry 1280x800 :%i"
                  "ExecStop=/usr/bin/vncserver -kill :%i"
                  ""
                  "[Install]"
                  "WantedBy=multi-user.target"
                  ""]))
    (actions/exec-checked-script
      "enable-and-start-vnc-service"
      ("systemctl" "daemon-reload")
      ("systemctl" "enable" ~vnv-service-name)
      ("systemctl" "start" ~vnv-service-name))))

(s/defn configure-user-tightvnc-server-script
  "Install remote desktop viewing."
  [facility :- s/Keyword
   user-name :- s/Str
   tightvnc :- Tightvnc]
  (let [{:keys [user-password]} tightvnc]
    (actions/as-action
     (logging/info (str facility "-configure user: tightvnc")))
    (set-user-password user-name user-password)))

(s/defn install-system
  [facility :- s/Keyword
   contains-tightvnc?]
  (when contains-tightvnc?
    (install-system-tightvnc-server facility)))

(s/defn install-user
  [facility :- s/Keyword
   user-name :- s/Str
   contains-tightvnc? :- s/Bool
   tightvnc :- Tightvnc]
  (when contains-tightvnc?
    (actions/as-action
     (logging/info (str facility "-install user: tightvnc")))
    (install-user-tightvnc-server facility user-name tightvnc)
    (install-user-vnc-tab-workaround facility user-name)))

(s/defn configure-user
  [facility :- s/Keyword
   user-name :- s/Str
   contains-tightvnc? :- s/Bool
   tightvnc :- Tightvnc]
  (when contains-tightvnc?
    (configure-system-tightvnc-server facility user-name)
    (configure-user-tightvnc-server-script facility user-name tightvnc)))
