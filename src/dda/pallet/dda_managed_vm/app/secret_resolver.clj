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
(ns dda.pallet.dda-managed-vm.app.secret-resolver
  (:require
    [schema.core :as s]
    [dda.pallet.dda-managed-vm.domain :as domain]
    [dda.pallet.commons.passwordstore-adapter :as adapter]))

;TODO: move secret to config.commons
;TODO: move passwordstore-adapter to config.commons
;TODO: move secret resolver to config.commons

(s/defn dispatch-by-secret-type :- s/Keyword
  "Dispatcher for secret resolving. Also does a
   schema validation of arguments."
  [secret :- domain/Secret]
  (first (keys secret)))

(defmulti resolve-secret
  "resolves the secret"
  dispatch-by-secret-type)
(s/defmethod resolve-secret :default
  [secret :- domain/Secret]
  (throw (UnsupportedOperationException. "Not impleneted yet.")))

(s/defmethod resolve-secret :plain
  [secret :- domain/Secret]
  (:plain secret))
(s/defmethod resolve-secret :password-store-single
  [secret :- domain/Secret]
  (adapter/get-secret-wo-newline (:password-store-single secret)))
(s/defmethod resolve-secret :password-store-multi
  [secret :- domain/Secret]
  (adapter/get-secret (:password-store-multi secret)))
