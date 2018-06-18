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
    [schema.core :as s]
    [pallet.actions :as actions]))

(def Settings
  "The basic settings"
  (hash-set (s/enum :install-virtualbox-guest :remove-power-management
                    :install-xfce-desktop  :install-os-analysis
                    :install-keymgm)))

(defn install-desktop-wiki
  []
  ;TODO: add "GTK2_RC_FILES=/usr/share/themes/Raleigh/gtk-2.0/gtkrc" in .bashrc
  (actions/packages
    :aptitude ["zim" "graphviz" "ditaa" "scrot"
               "dvipng" "gnuplot" "r-base" "python-gtkspellcheck"
               "aspell" "aspell-de"]))
