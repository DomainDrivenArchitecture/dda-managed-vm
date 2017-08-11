; Copyright (c) meissa GmbH. All rights reserved.
; You must not remove this notice, or any other, from this software.

(ns dda.pallet.dda-managed-vm.infra.mozilla
  (:require
   [schema.core :as s]
   [pallet.actions :as actions]))

(def Link [(s/one s/Str "url")  (s/one s/Str "name")])

(def Folder {:name s/Str
             (s/optional-key :links) [Link]
             (s/optional-key :childs) [(s/recursive #'Folder)]})

(def Bookmarks [Folder])

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
  [user :- s/Str
   folders :- Bookmarks]
  (let [content (clojure.string/join
                  \newline
                  (generate-bookmarks folders))]
    (actions/remote-file
      (str "/home/" user "/bookmarks.html")
      :literal true
      :content content)))
