; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns org.domaindrivenarchitecture.pallet.crate.managed-vm.tightvnc
  (:require
    [pallet.actions :as actions]
    [pallet.crate.git :as git]
    [pallet.stevedore :as stevedore]
    [org.domaindrivenarchitecture.pallet.crate.util :as util]
    [org.domaindrivenarchitecture.pallet.crate.package :as dda-package]
    ))

(defn set-user-password [os-user password]
   (let [vnc-path (str "/home/" os-user "/.vnc")]
     (actions/exec-checked-script
         "set-vnc-user-password"
         (pipe (println ~password) ("vncpasswd" -f > ~(str vnc-path "/passwd")))
         ("chown" "-R" ~(str os-user ":" os-user) ~vnc-path)
         ("chmod" "0600" ~(str vnc-path "/passwd"))
         ))
  )

(defn install-system-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (actions/package "tightvncserver"))

(defn install-user-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (let [os-user (name (-> config :ide-user))
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
                  "xrdb $HOME/.Xresources"
                  "startxfce4 &"]))
    (set-user-password os-user password)
    ))

(defn configure-system-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (let [os-user (name (-> config :ide-user))
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
      ("systemctl" "start" ~vnv-service-name))
    ))

(defn configure-user-tightvnc-server
  "Install remote desktop viewing."
  [config]
  (let [os-user (name (-> config :ide-user))
        password (-> config :tightvnc-server :user-password)
        vnc-path (str "/home/" os-user "/.vnc")
        vnv-service-name (str "vncserver@" os-user ".service")]
    (set-user-password os-user password)
    ))
