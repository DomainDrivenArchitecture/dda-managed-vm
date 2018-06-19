(defproject dda/dda-managed-vm "2.0.1-SNAPSHOT"
  :description "The managed vm desktop crate"
  :url "https://www.domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[dda/dda-config-commons "1.4.0"]
                 [dda/dda-pallet "2.2.0"]
                 [dda/dda-user-crate "1.0.5"]
                 [dda/dda-git-crate "1.1.4"]
                 [dda/dda-backup-crate "1.0.4"]
                 [dda/dda-serverspec-crate "1.1.1"]]
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :repositories [["snapshots" :clojars]
                 ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:dev {:source-paths ["integration/src"
                                  "test/src"
                                  "uberjar/src"]
                   :resource-paths ["integration/resources"
                                    "test/resources"]
                   :dependencies
                   [[org.clojure/test.check "0.10.0-alpha2"]
                    [org.domaindrivenarchitecture/pallet-aws "0.2.8.2"
                     :exclusions [com.palletops/pallet]]
                    [dda/pallet "0.9.0" :classifier "tests"]
                    [dda/dda-pallet-commons "1.3.1" :classifier "tests"]
                    [ch.qos.logback/logback-classic "1.3.0-alpha4"]
                    [org.slf4j/jcl-over-slf4j "1.8.0-beta1"]]
                   :plugins
                   [[lein-sub "0.3.0"]]
                   :leiningen/reply
                   {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-beta0"]]
                    :exclusions [commons-logging]}
                   :repl-options {:init-ns dda.pallet.dda-managed-vm.app.instantiate-existing}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.0" :classifier "tests"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main dda.pallet.dda-managed-vm.main
                       :dependencies [[org.clojure/tools.cli "0.3.7"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha4"]
                                      [org.slf4j/jcl-over-slf4j "1.8.0-beta1"]]}}
  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag"]
                  ["deploy"]
                  ["uberjar"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]]
  :local-repo-classpath true)
