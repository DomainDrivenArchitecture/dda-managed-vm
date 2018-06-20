# dda-managed-vm
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-managed-vm.svg)](https://clojars.org/dda/dda-managed-vm)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm)

[![Slack](https://img.shields.io/badge/chat-clojurians-green.svg?style=flat)](https://clojurians.slack.com/messages/#dda-pallet/) | [<img src="https://domaindrivenarchitecture.org/img/meetup.svg" width=50 alt="DevOps Hacking with Clojure Meetup"> DevOps Hacking with Clojure](https://www.meetup.com/de-DE/preview/dda-pallet-DevOps-Hacking-with-Clojure) | [Website & Blog](https://domaindrivenarchitecture.org)

## Compatibility

This crate works with:
 * pallet 0.9
 * clojure 1.9
 * xubuntu 18.04

## Features

This crate automatically installs software on a Linux system. It can be a standalone system, but normally would be a virtual machine. For this reason we usually refer to the system as "virtual machine" in the text below.

The following software/packages are installed by this dda-managed-vm:
 * Teamable desktop-wiki: zim, a desktop wiki stores its content in plain text files and is backed by autosynchronized git repositories.
 * Gpg key & ssh key: Optionally, you can install your gpg- and/or ssh keys.
 * Team-able passwordstore: Store your passwords using gopass or passwordstore, encrypted by gpg and versioned by git. For more details see: https://www.passwordstore.org/ and https://github.com/DomainDrivenArchitecture/password-store-for-teams. In order to test you can use:
   ```
   demo-pass                       #see all passwords stored
   demo-pass testuser/demo-secret  #decrypt the demo-secret. Works, if you've installed the snakeoil key.
   ```
 * Tightvnc-server (experimental): Allows you to connect to your vm and control it with a vnc tool (eg. gtk-vnc) using:
   ```
    server: [ip]:5091
    account: [vm-user]
    vnc-password: test
   ```
   You can find a configuration example here: `integration/resources/snakeoil-vm-remote.edn`
 * Browser & Bookmarks: Some bookmarks are installed in `~/bookmark.html`, which you can import in your browser, e.g. Firefox or Google Chrome.
 * Git & git repos: You can install git repositories & servertrust.
 * Java JRE 1.8 / 11
 * LibreOffice & SpellChecking
 * htop, iotop, iftop, strace, mtr in case of low level debugging.
 * VirtualBox Tools: Are installed by apt-get and will be updated automatically.
 * power-management, swappiness

## Usage documentation
This crate installs and configures software on your virtual machine. You can provision pre-created virtual machines (see paragraph "Prepare vm" below) or cloud instances.

### Prepare vm
If you want to use this crate, please ensure you meet the preconditions for the remote machine, i.e. xubuntu and openssh-server installed. If not yet installed, you may use the steps below:
1. Install xubuntu18.04
2. Login with your initial user and use:
```
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install openssh-server
```
In case you want to install the software on the local machine rahter than remote, you wouldn't need openssh-server but only a Java runtime environment. If not yet available, you can install Java by:
```
sudo apt-get install openjdk-11-jre-headless
```


### Usage Summary
1. Download the jar-file from the releases page of this repository (e.g. dda-manage-vm-x.x.x-standalone.jar).
2. Deploy the jar-file on the source machine
3. Create the files `example-vm.edn` (Domain-Schema for your desktop) and `example-target.edn` (Schema for Targets to be provisioned) according to the reference and our example configurations. Please create them in the same folder where you've saved the jar-file. For more information about these files refer to the corresponding information below.
4. Start the installation:
```bash
java -jar dda-managed-vm-standalone.jar --targets example-targets.edn example-vm.edn
```

### Configuration
The configuration consists of two files defining both WHERE to install the software and WHAT to install.
* `example-targets.edn`: describes on which target system(s) the software will be installed
* `example-vm.edn`: describes which software/packages will be installed

You can download examples of these configuration files from [https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/example-targets.edn](https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/example-targets.edn) and [https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/example-vm.edn](https://github.com/DomainDrivenArchitecture/dda-managed-vm/blob/development/example-vm.edn) respectively.

#### Targets config example
Example content of file `example-targets.edn`:
```clojure
{:existing [{:node-name "test-vm1"            ; semantic name
             :node-ip "35.157.19.218"}]       ; the ip4 address of the machine to be provisioned
 :provisioning-user {:login "initial"         ; account used to provision
                     :password "secure1234"}} ; optional password, if no ssh key is authorized
```

#### VM config example
Example content of file `example-vm.edn`:
```clojure
{:target-type :virtualbox
 :usage-type :desktop-office
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

### Targets
The schema for the targets config is:
```clojure
(def ExistingNode
  "Represents a target node with ip and its name."
  {:node-name s/Str   ; semantic name (keep the default or use a name that suits you)
   :node-ip s/Str})   ; the ip4 address of the machine to be provisioned

(def ExistingNodes
  "A sequence of ExistingNodes."
  {s/Keyword [ExistingNode]})

(def ProvisioningUser
  "User used for provisioning."
  {:login s/Str                                ; user on the target machine, must have sudo rights
   (s/optional-key :password) secret/Secret})  ; password can be ommited, if a ssh key is authorized

(def Targets
  "Targets to be used during provisioning."
  {:existing [ExistingNode]                                ; one ore more target nodes.
   (s/optional-key :provisioning-user) ProvisioningUser})  ; user can be ommited to execute on localhost with current user.
```
The "example-targets.edn" uses this schema.

### Domain API
[see domain reference](doc/reference_domain.md)

### Infra API
[see infra reference](doc/reference_infra.md)

## License
Published under [apache2.0 license](LICENSE.md)
