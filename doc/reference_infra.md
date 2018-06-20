The Infra configuration is a configuration on the infrastructure level of a crate. It contains the complete configuration options that are possible with the crate functions. You can find the details of the infra configurations at the other crates used:
* [dda-user-crate](https://github.com/DomainDrivenArchitecture/dda-user-crate)
* [dda-git-crate](https://github.com/DomainDrivenArchitecture/dda-git-crate)
* [dda-serverspec-crate](https://github.com/DomainDrivenArchitecture/dda-serverspec-crate)

For installation & configuration with the dda-managed-vm the schema is:
```clojure
(def DdaVmConfig {
  {:vm-user s/Keyword                                           ; user-name
   (s/optional-key :tightvnc-server) {:user-password s/Str}     ; install vnc?
   (s/optional-key :bookmarks) Bookmarks
   (s/optional-key :settings)
   (hash-set (s/enum :install-virtualbox-guest :remove-power-management
                     :install-xfce-desktop  :install-os-analysis
                     :install-keymgm :configure-no-swappiness
                     :install-password-store :install-gopass
                     :install-desktop-wiki
                     :install-open-jdk-8 :install-open-jdk-11
                     :install-libreoffice :install-spellchecking-de))})
```