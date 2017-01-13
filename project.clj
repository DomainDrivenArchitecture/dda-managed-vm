(defproject org.domaindrivenarchitecture/dda-managed-vm "0.1.2-SNAPSHOT"
  :description "The managed vm desktop crate"
  :url "https://www.domaindrivenarchitecture.org"
  :license {:name "Apache License, Version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [com.palletops/pallet "0.8.12" :exclusions [org.clojure/tools.cli]]
                 [com.palletops/stevedore "0.8.0-beta.7"]
                 [prismatic/schema "1.1.3"]
                 [com.palletops/git-crate "0.8.0-alpha.2" :exclusions [org.clojure/clojure]]
                 [org.domaindrivenarchitecture/dda-pallet "0.3.1-SNAPSHOT"]
                 [org.domaindrivenarchitecture/dda-pallet-commons "0.3.0"]
                 [org.domaindrivenarchitecture/dda-user-crate "0.3.5-SNAPSHOT"]
                 [org.domaindrivenarchitecture/dda-init-crate "0.2.1-SNAPSHOT"]
                 [org.domaindrivenarchitecture/dda-backup-crate "0.4.1-SNAPSHOT"]]
  :repositories [["snapshots" :clojars]
                  ["releases" :clojars]]
  :deploy-repositories [["snapshots" :clojars]
                        ["releases" :clojars]]
  :profiles {:uberjar 
             {:aot :all
              :main main}
             :dev
             {:source-paths ["integration"]
              :resource-paths ["dev-resources"]
              :dependencies
              [[org.clojure/test.check "0.9.0"]
               [com.palletops/pallet "0.8.12" :classifier "tests"]
               [org.domaindrivenarchitecture/dda-pallet-commons "0.3.0" :classifier "tests"]
               [org.domaindrivenarchitecture/pallet-aws "0.2.8-SNAPSHOT"]
               [ch.qos.logback/logback-classic "1.1.8"]
               [org.slf4j/jcl-over-slf4j "1.7.22"]]
              :plugins
              [[com.palletops/pallet-lein "0.8.0-alpha.1"]
               [lein-sub "0.3.0"]]}
              :leiningen/reply
               {:dependencies [[org.slf4j/jcl-over-slf4j "1.7.22"]]
                :exclusions [commons-logging]}}
  :local-repo-classpath true
  :classifiers {:tests {:source-paths ^:replace ["test" "integration"]
                        :resource-paths ^:replace ["dev-resources"]}}
  )