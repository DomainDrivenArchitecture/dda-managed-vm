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


(ns dda.pallet.dda-managed-vm.infra.browser-test
  (:require
    [clojure.test :refer :all]
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.infra.browser :as sut]))


(def expected-result
  ["<!DOCTYPE NETSCAPE-Bookmark-file-1>"
   "<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">"
   "<TITLE>Bookmarks</TITLE>"
   "<H1>Bookmarks Menu</H1>"
   "<DL><p>"
   "    <DT><H3 PERSONAL_TOOLBAR_FOLDER=\"true\">Bookmarks Toolbar</H3>"
   "    <DL><p>"
   "        <DT><A HREF=\"https://domaindrivenarchitecture.org/\">dda</A>"
   "        <DT><H3>WebConf</H3>"
   "        <DL><p>"
   "            <DT><A HREF=\"https://meet.jit.si/dda-pallet\">jitsi dda-pallet</A>"
   "            <DT><A HREF=\"http://meetingwords.com/\">MeetingWords</A>"
   "            <DT><A HREF=\"https://web.telegram.org/\">Telegram</A>"
   "            <DT><A HREF=\"http://www.meebl.de/\">meebl</A>"
   "        </DL><p>"
   "    </DL><p>"
   "</DL>"])

(def bookmarks-config [{:name "Bookmarks Toolbar"
                        :links [["https://domaindrivenarchitecture.org/" "dda"]]
                        :childs [{:name "WebConf"
                                  :links [["https://meet.jit.si/dda-pallet" "jitsi dda-pallet"]
                                          ["http://meetingwords.com/" "MeetingWords"]
                                          ["https://web.telegram.org/" "Telegram"]
                                          ["http://www.meebl.de/" "meebl"]]}]}])

(deftest test-bookmarks-generation
  (testing
    (is (= expected-result
         (sut/generate-bookmarks bookmarks-config)))))
