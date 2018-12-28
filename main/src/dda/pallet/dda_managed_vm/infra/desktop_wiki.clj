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
(ns dda.pallet.dda-managed-vm.infra.desktop-wiki
  (:require
    [clojure.tools.logging :as logging]
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  "The basic settings"
  (hash-set
    :install-diagram
    :install-desktop-wiki))

(defn init-desktop-wiki
  [facility]
  (actions/as-action
   (logging/info (str facility "-init system: init-desktop-wiki")))
  (actions/package-source "zim"
    :aptitude
    {:url "http://ppa.launchpad.net/jaap.karssenberg/zim/ubuntu"
     :release "bionic"
     :scopes ["main"]
     :key-url "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x7588B93F8F7DF243"}))

(defn install-diagram
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-diagram")))
  (actions/package-manager :update)
  (actions/packages
    :aptitude ["graphviz" "ditaa" "scrot" "dia"
               "dvipng" "gnuplot" "r-base"]))

(defn install-desktop-wiki
  [facility]
  (actions/as-action
   (logging/info (str facility "-install system: install-desktop-wiki")))
  (actions/packages
    :aptitude ["zim" "python-gtkspellcheck" "aspell" "aspell-de"]))

(s/defn init-system
  [facility settings]
  (when (contains? settings :install-desktop-wiki)
    (init-desktop-wiki facility)))

(s/defn install-system
  [facility settings]
  (when (contains? settings :install-diagram)
    (install-diagram facility))
  (when (contains? settings :install-desktop-wiki)
    (install-desktop-wiki facility)))
