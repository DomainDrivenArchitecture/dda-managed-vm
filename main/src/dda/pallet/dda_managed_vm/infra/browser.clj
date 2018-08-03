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
(ns dda.pallet.dda-managed-vm.infra.browser
  (:require
   [clojure.tools.logging :as logging]
   [schema.core :as s]
   [pallet.actions :as actions]
   [dda.config.commons.user-home :as user-env]))

(def Link [(s/one s/Str "url")  (s/one s/Str "name")])

(def Folder {:name s/Str
             (s/optional-key :links) [Link]
             (s/optional-key :childs) [(s/recursive #'Folder)]})

(def Bookmarks [Folder])

(def Settings
  "The browser settings"
  (hash-set
    :install-chromium))

(s/defn install-chromium
  [facility :- s/Keyword]
  (actions/as-action
    (logging/info (str facility "-install system: chromium")))
  (actions/package "chromium-browser"))

(def header
  ["<!DOCTYPE NETSCAPE-Bookmark-file-1>"
   "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">"
   "<TITLE>Bookmarks</TITLE>"
   "<H1>Bookmarks Menu</H1>"
   "<DL><p>"])

(def tail
   ["</DL>"])

(s/defn indent :- s/Str
  [level :- s/Num]
  (apply str (take level (repeat "    "))))

(s/defn generate-single-link
 [level :- s/Num
  link :- Link]
 (str
   (indent level)
   "<DT><A HREF=\"" (first link) "\">" (second link) "</A>"))

(s/defn generate-single-folder
 [level :- s/Num
  folder :- Folder]
 (let [{:keys [name links childs]} folder
       toolbar-folder? (= "Bookmarks Toolbar" name)]
   (concat
      [(str
          (indent level)
          (if toolbar-folder?
            "<DT><H3 PERSONAL_TOOLBAR_FOLDER=\"true\">"
            "<DT><H3>")
          name "</H3>")
       (if (or links childs)
          [(str (indent level) "<DL><p>")]
          [])
       (if links
         (flatten (map (partial generate-single-link (+ 1 level)) links))
         [])
       (if childs
         (flatten (map (partial generate-single-folder (+ 1 level)) childs))
         [])
       (if (or links childs)
         [(str (indent level) "</DL><p>")]
         [])])))

(s/defn generate-bookmarks
  "generaters bookmarks html lines"
  [folders :- Bookmarks]
  (concat
    header
    (flatten (map (partial generate-single-folder 1) folders))
    tail))

(s/defn configure-user-bookmarks
  ""
  [facility :- s/Keyword
   user-name :- s/Str
   folders :- Bookmarks]
  (let [content (clojure.string/join
                  \newline
                  (generate-bookmarks folders))]
    (actions/as-action
     (logging/info (str facility "-configure user: bookmarks")))
    (actions/remote-file
      (str (user-env/user-home-dir user-name) "/bookmarks.html")
      :literal true
      :owner user-name
      :group user-name
      :content content)))

(s/defn install-system
  [facility :- s/Keyword
   settings]
  (when (contains? settings :install-chromium)
    (install-chromium facility)))

(s/defn configure-user
  [facility :- s/Keyword
   user-name :- s/Str
   contains-bookmarks?
   bookmarks]
  (when contains-bookmarks?
    (configure-user-bookmarks facility user-name bookmarks)))
