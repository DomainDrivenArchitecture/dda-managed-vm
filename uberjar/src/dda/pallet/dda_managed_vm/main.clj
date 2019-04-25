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
;[dda.pallet.commons.cli-helper :as cli-helper]

(ns dda.pallet.dda-managed-vm.main
  (:gen-class)
  (:require
    [clojure.string :as str]
    [clojure.tools.cli :as cli]
    [dda.pallet.core.main-helper :as mh]
    [dda.pallet.core.app :as core-app]
    [dda.pallet.dda-managed-vm.app :as app]))

(def cli-options
  [["-h" "--help"]
   ["-s" "--serverspec"]
   ["-c" "--configure"]
   ["-t" "--targets targets.edn" "edn file containing the targets to install on."
    :default "localhost-target.edn"]])

(defn usage [options-summary]
  (str/join
   \newline
   ["dda-managed-vm installs a variety of standard software and configs on your personal vm"
    ""
    "Usage: java -jar dda-managed-vm-[version]-standalone.jar [options] vm-spec-file"
    ""
    "Options:"
    options-summary
    ""
    "vm-spec-file"
    "  - follows the edn format."
    "  - has to be a valid DdaVmDomainConfig (see: https://github.com/DomainDrivenArchitecture/dda-managed-vm)"
    ""]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary help]} (cli/parse-opts args cli-options)]
    (cond
      help (mh/exit 0 (usage summary))
      errors (mh/exit 1 (mh/error-msg errors))
      (not= (count arguments) 1) (mh/exit 1 (usage summary))
      (:serverspec options) (if (core-app/existing-serverspec
                                  app/crate-app
                                  {:domain (first arguments)
                                   :targets (:targets options)})
                              (mh/exit-test-passed)
                              (mh/exit-test-failed))
      (:configure options) (if (core-app/existing-configure
                                 app/crate-app
                                 {:domain (first arguments)
                                  :targets (:targets options)})
                               (mh/exit-default-success)
                               (mh/exit-default-error))
      :default (let [result (core-app/existing-install
                              app/crate-app
                              {:domain (first arguments)
                               :targets (:targets options)})]
                 (if result
                   (mh/exit-default-success)
                   (mh/exit-default-error))))))
