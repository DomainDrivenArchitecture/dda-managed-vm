# dda-managed-vm
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-managed-vm.svg)](https://clojars.org/dda/dda-managed-vm)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## compatability

This crate is working with:
 * pallet 0.8
 * clojure 1.7
 * xubuntu 16.04.02

## Features
### VirtualBox Tools
are installed by apt-get and will be updated automatically.

### Browser & Bookmarks
You can seed some bookmarks in ~/bookmark.html in order to import in ffox or chromium.

### Team able passwordstore
Store your passwords encrypted by gpg and versioned by git.
For more details see: https://www.passwordstore.org/ and https://github.com/DomainDrivenArchitecture/password-store-for-teams
In order to test you can
```
demo-pass                       #see all passwords stored
demo-pass testuser/demo-secret  # decrypt the demo-secret. Works if youve installed the snakeoil key
```
### tightvnc-server (experimental)
You may connect to your vm using a vnc tool (eg. gtk-vnc) using:
```
 server: [ip]:5091
 account: [vm-user]
 vnc-password: test
```
You can find a configuration example here: `integration/resources/snakeoil-vm-remote.edn`

### gpg key & ssh key
As part of dda-user-crate your gpg- and ssh key can be installed.

### git & git repos
As part of dda-git-crate you can preinstall git repositories & servertrust.

### More Software
* Java JRE 1.8
* LibreOffice & SpellChecking
* htop, iotop, iftop, strace, mtr in case of low level debugging.

## Usage documentation
This crate installs and configures your desktop (-vm). You can provision precreated desktops (see Prepare VM) or cloud instances.

### Prepare vm (optional)
1. install xubuntu16.04.02
2. login with your initial user
```
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install openssh-server openjdk-7-jre-headless
```

### Usage Summary
1. Download the jar from the releases page of this repository
2. Deploy jar on the source machine
3. Adjust vm.edn (Domain-Schema for your desktop) and target.edn (Schema for Targets to be provisioned) according to the reference and our example configs
4. Start installation:
```bash
java -jar dda-managed-vm-standalone.jar --targets targets.edn vm.edn
```

### Configuration
Configuraion consist in two files,
* `targets.edn`: describe the target to be provisioned and
* `vm.edn`: describe your desktop-vm to be installed

#### Targets config Example
```clojure
{:existing [{:node-name "test-vm1"            ; semantic name
             :node-ip "35.157.19.218"}]       ; the ip4 adress to be provisioned
 :provisioning-user {:login "initial"          ; account used to provision
                     :password "secure1234"}} ; optional password, if no ssh key is authorized
```

#### VM config Example
```clojure
{:type :desktop-office
 :user {:name "test-user"
        :password {:plain "xxx"}
        :email "test-user@mydomain.org"
        :ssh {:ssh-public-key {:plain "rsa-ssh kfjri5r8irohgn...test.key comment"}
              :ssh-private-key {:plain "123Test"}}}
       :gpg {:gpg-public-key
             {:plain "-----BEGIN PGP ...."
              :gpg-private-key
              {:plain "-----BEGIN PGP ...."}
              :gpg-passphrase {:plain "passphrase"}}}}
```         

The vm config determines the vm-type and user credentials to be installed.

### watch log for debug reasons
`less logs/pallet.log`

## Reference
We provide two levels of API - domain is a high level API with many build in conventions. If this conventions doe not fit your needs, you can use our low-level (infra) API and realize your own conventions.

### Domain API

#### Targets
The schema is:
```clojure
(def ExistingNode {:node-name Str                   ; your name for the node
                   :node-ip Str                     ; nodes ip4 address       
                   })

(def ProvisioningUser {:login Str                   ; user account used for provisioning / executing tests
                       (optional-key :password) Str ; password, is no authorized ssh key is avail.
                       })

(def Targets {:existing [ExistingNode]              ; nodes to test or install
              :provisioning-user ProvisioningUser   ; common user account on all nodes given above
              })
```
The "targets.edn" has the schema of the Targets

#### Tests
The schema is:
```clojure
(def Bookmarks
  [{(optional-key :childs) [(recursive
                           (var
                            dda.pallet.dda-managed-vm.infra.mozilla/Folder))],
  :name Str,
  (optional-key :links) [[(one Str "url") (one Str "name")]]}])

(def Secret                                       ; secrets can be given
  { (optional-key :plain) Str,                    ;   as plain text
    (optional-key :password-store-single) Str,    ;   as password store key wo linebreaks
    (optional-key :password-store-multi) Str})    ;   as password store key with linebreaks

{:type (enum :remote :desktop-office :desktop-minimal),             ; remote: all featured software, no vbox-guest-utils
                                                                    ; desktop-office: vbox-guest utils, all featured software, no vnc
                                                                    ; desktop-minimal: just vbox-guest-utils, no java
 (optional-key :bookmarks) Bookmarks,                               ; initial bookmarks
 :user {:name s/Str                                                 ; user with his credentials
        :password Secret
        (s/optional-key :email) s/Str
        (s/optional-key :ssh) {:ssh-public-key Secret
                               :ssh-private-key Secret}
        (s/optional-key :gpg) {:gpg-public-key Secret
                               :gpg-private-key Secret
                               :gpg-passphrase Secret}}
        (optional-key :email) Str}}                                 ; email for git config
```

For `Secret` there can be found more adapters in dda-palet-commons.

### Infra API
The Infra configuration is a configuration on the infrastructure level of a crate. It contains the complete configuration options that are possible with the crate functions. You can find the infra reference of used crates here:
* dda-user-crate
* dda-git-crate
* dda-serverspec-crate

For vm installation & configuration the schema is:
```clojure
(def DdaVmConfig {
  {:vm-user s/Keyword                                           ; user-name
   (s/optional-key :tightvnc-server) {:user-password s/Str}     ; install vnc?
   (s/optional-key :bookmarks) Bookmarks                        
   (s/optional-key :settings)
   (hash-set (s/enum :install-virtualbox-guest :install-libreoffice
                     :install-open-jdk-8 :install-xfce-desktop
                     :install-analysis :install-git :install-password-store))})
```

## License
Published under [apache2.0 license](LICENSE.md)
