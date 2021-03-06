(defproject dda/dda-managed-vm "2.6.6"
  :description "The managed vm desktop crate"
  :url "https://www.domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[dda/dda-pallet "3.1.2"]
                 [dda/dda-serverspec-crate "1.3.4"]
                 [dda/dda-user-crate "2.1.2"]
                 [dda/dda-git-crate "2.2.5"]
                 [dda/dda-backup-crate "1.0.7"]]
  :source-paths ["main/src"]
  :resource-paths ["main/resources"]
  :target-path "target/%s/"
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
                   [[org.clojure/test.check "1.1.0"]
                    [dda/pallet "0.9.1" :classifier "tests"]
                    [dda/data-test "0.1.1"]
                    [dda/dda-pallet-commons "1.6.4" :classifier "tests"]
                    [ch.qos.logback/logback-classic "1.3.0-alpha5"]
                    [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]
                   :plugins
                   [[lein-sub "0.3.0"]]
                   :leiningen/reply
                   {:dependencies [[org.slf4j/jcl-over-slf4j "1.8.0-beta0"]]
                    :exclusions [commons-logging]}
                   :repl-options {:init-ns dda.pallet.dda-managed-vm.app.instantiate-existing}}
             :test {:test-paths ["test/src"]
                    :resource-paths ["test/resources"]
                    :dependencies [[dda/pallet "0.9.1" :classifier "tests"]]}
             :uberjar {:source-paths ["uberjar/src"]
                       :resource-paths ["uberjar/resources"]
                       :aot :all
                       :main dda.pallet.dda-managed-vm.main
                       :uberjar-name "dda-managed-vm-standalone.jar"
                       :dependencies [[org.clojure/tools.cli "1.0.194"]
                                      [ch.qos.logback/logback-classic "1.3.0-alpha5"
                                       :exclusions [com.sun.mail/javax.mail]]
                                      [org.slf4j/jcl-over-slf4j "2.0.0-alpha1"]]}}
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
