# dda-managed-vm
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-managed-vm.svg)](https://clojars.org/dda/dda-managed-vm)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## Compatibility

This crate works with:
 * pallet 0.8
 * clojure 1.7
 * xubuntu 16.04.02

## Features

This crate automatically installs software on a Linux system. It can be a standalone system, but normally would be a virtual machine. For this reason we usually refer to the system as "virtual machine" in the text below.

The following software/packages are installed by this dda-managed-vm:

### VirtualBox Tools
are installed by apt-get and will be updated automatically.

### Gpg key & ssh key
Optionally, you can install your gpg- and/or ssh keys.

### Team-able passwordstore
Store your passwords in the passwordstore, encrypted by gpg and versioned by git.
For more details see: https://www.passwordstore.org/ and https://github.com/DomainDrivenArchitecture/password-store-for-teams.
In order to test you can use:
```
demo-pass                       #see all passwords stored
demo-pass testuser/demo-secret  #decrypt the demo-secret. Works, if you've installed the snakeoil key.
```
### Tightvnc-server (experimental)
Tightvnc software allows you to connect to your vm and control it with a vnc tool (eg. gtk-vnc) using:
```
 server: [ip]:5091
 account: [vm-user]
 vnc-password: test
```
You can find a configuration example here: `integration/resources/snakeoil-vm-remote.edn`

### Browser & Bookmarks
Some bookmarks are installed in `~/bookmark.html`, which you can import in your browser, e.g. Firefox or Google Chrome.

### Git & git repos
You can install git repositories & servertrust.

### More Software
* Java JRE 1.8
* LibreOffice & SpellChecking
* htop, iotop, iftop, strace, mtr in case of low level debugging.

## Usage documentation
This crate installs and configures software on your virtual machine. You can provision pre-created virtual machines (see paragraph "Prepare vm" below) or cloud instances.

### Prepare vm
If you want to use this crate, please ensure you meet the preconditions for the remote machine, i.e. xubuntu and openssh-server installed. If not yet installed, you may use the steps below:
1. Install xubuntu16.04.02
2. Login with your initial user and use:
```
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install openssh-server
```
In case you want to install the software on the local machine rahter than remote, you wouldn't need openssh-server but only a Java runtime environment. If not yet available, you can install Java by:
```
sudo apt-get install openjdk-7-jre-headless
```


### Usage Summary
1. Download the jar-file from the releases page of this repository (e.g. dda-manage-vm-x.x.x-standalone.jar).
2. Deploy the jar-file on the source machine
3. Create the files `vm.edn` (Domain-Schema for your desktop) and `target.edn` (Schema for Targets to be provisioned) according to the reference and our example configurations. Please create them in the same folder where you've saved the jar-file. For more information about these files refer to the corresponding information below.
4. Start the installation:
```bash
java -jar dda-managed-vm-standalone.jar --targets targets.edn vm.edn
```

### Configuration
The configuration consists of two files defining both WHERE to install the software and WHAT to install.
* `targets.edn`: describes on which target system(s) the software will be installed
* `vm.edn`: describes which software/packages will be installed

You can download examples of these configuration files from [https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/targets.edn](https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/targets.edn) and [https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/vm.edn](https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/vm.edn) respectively.

#### Targets config example
Example content of file `targets.edn`:
```clojure
{:existing [{:node-name "test-vm1"            ; semantic name
             :node-ip "35.157.19.218"}]       ; the ip4 address of the machine to be provisioned
 :provisioning-user {:login "initial"         ; account used to provision
                     :password "secure1234"}} ; optional password, if no ssh key is authorized
```

#### VM config example
Example content of file `vm.edn`:
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

The vm config defines the software/packages and user credentials of the newly created user to be installed.

### Watch log for debug reasons
In case of problems you may want to have a look at the log-file:
`less logs/pallet.log`

## Reference
Some details about the architecture: We provide two levels of API. **Domain** is a high-level API with many build in conventions. If this conventions don't fit your needs, you can use our low-level **infra** API and realize your own conventions.

### Domain API

#### Targets
The schema for the targets config is:
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
The "targets.edn" uses this schema.

#### VM config
The schema for the vm configuration is:
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

For `Secret` you can find more adapters in dda-palet-commons.

### Infra API
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
   (hash-set (s/enum :install-virtualbox-guest :install-libreoffice
                     :install-spellchecking :install-open-jdk-8
                     :install-xfce-desktop  :install-analysis
                     :install-keymgm :install-git
                     :install-password-store))})
```

## License
Published under [apache2.0 license](LICENSE.md)
