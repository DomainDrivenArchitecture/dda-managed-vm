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
    [pallet.actions :as actions]
    [dda.pallet.crate.util :as util]))

(defn set-user-password [os-user password]
   (let [vnc-path (str "/home/" os-user "/.vnc")]
     (actions/exec-checked-script
         "set-vnc-user-password"
         (pipe (println ~password) ("vncpasswd" -f > ~(str vnc-path "/passwd")))
         ("chown" "-R" ~(str os-user ":" os-user) ~vnc-path)
         ("chmod" "0600" ~(str vnc-path "/passwd")))))

(defn install-system-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (actions/package "tightvncserver"))

(defn install-user-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (let [os-user (name (-> config :vm-user))
        password (-> config :tightvnc-server :user-password)
        vnc-path (str "/home/" os-user "/.vnc")
        vnv-service-name (str "vncserver@" os-user ".service")]
    (actions/directory vnc-path :owner os-user :group os-user)
    (actions/remote-file
      (str vnc-path "/xstartup")
      :owner os-user
      :group os-user
      :mode "0700"
      :literal true
      :content (util/create-file-content
                 ["#!/bin/bash"
                  "source /etc/profile"
                  "xrdb $HOME/.Xresources"
                  "startxfce4 &"
                  "xfconf-query -c xfce4-keyboard-shortcuts -p /xfwm4/custom/'<'Super'>'Tab -r"]))))

(defn install-user-vnc-tab-workaround
  "Install a small script to fix tab issue on vnc."
  [config]
  (let [os-user (name (-> config :vm-user))
        script-path (str "/home/" os-user "/vnc-tab-workaround.sh")]
    (actions/remote-file
      script-path
      :owner os-user
      :group os-user
      :mode "0700"
      :literal true
      :content (util/create-file-content
                 ["#!/bin/bash"
                  "xfconf-query -c xfce4-keyboard-shortcuts -p /xfwm4/custom/'<'Super'>'Tab -r"]))))

(defn configure-system-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (let [os-user (name (-> config :vm-user))
        vnv-service-name (str "vncserver@1" ".service")]
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
                  (str "User=" os-user)
                  "PAMName=login"
                  (str "PIDFile=/home/" os-user "/.vnc/%H:%i.pid")
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

(defn configure-user-tightvnc-server-script
  "Install remote desktop viewing."
  [user-name config]
  (let [os-user user-name
        password (-> config :tightvnc-server :user-password)]
    (set-user-password os-user password)))
