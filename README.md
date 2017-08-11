# dda-managed-vm
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-managed-vm.svg)](https://clojars.org/dda/dda-managed-vm)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm)

Requirements can be found at https://dda.gitbooks.io/domaindrivenarchitecture/content/en/80_config_management/30_requirements/index.html

## compatability

This crate is working with:
 * pallet 0.8
 * clojure 1.7
 * ubuntu 16.04

## Features
 * VirtualBox Tools
 * Java JRE
 * Browser & Bookmarks
 * LibreOffice & SpellChecking

## Install & Configure by *.jar
 Use dda-managed-vm on order to install you personal vm.

### Build your own Installer & Configurator
1. Get your own clone
  1. git clone
2. Adjust your own configuration
  1. cd dda-managed-vm
  2. git checkout -b [your-personal-branch]
  3. Adjust your configuration at /dda-managed-vm/src/vm_config.clj
  4. ensure, that /src/main.clj has enabled localhost-node
3. Create your installer
  1. lein uberjar

### Prepare ide-vm
1. install xubuntu14.04.03
2. login with your initial user
3. sudo apt-get update
4. sudo apt-get upgrade
5. sudo apt-get install openssh-server openjdk-7-jre-headless
6. scp target/dda-managed-vm-0.1.0-SNAPSHOT-standalone.jar [your initial user]@[your vms ip]:
7. scp ~/.ssh/id_rsa* [your initial user]@[your vms ip]:

### install ide
1. sudo -i
2. cd /home/[your initial user]
3. java -jar dda-managed-vm-0.1.0-SNAPSHOT-standalone.jar init
4. java -jar dda-managed-vm-0.1.0-SNAPSHOT-standalone.jar install
5. java -jar dda-managed-vm-0.1.0-SNAPSHOT-standalone.jar configure

### watch log for debug reasons
1. less logs/pallet.log

## Install & Configure by ssh
 Use dda-managed-vm on order to install you personal vm remote using ssh.

### Build your own Installer & Configurator
1. Get your own clone
  1. git clone
2. Adjust your own configuration
  1. cd dda-managed-vm
  2. git checkout -b [your-personal-branch]
  3. Adjust your configuration at /dda-managed-vm/src/vm_config.clj
  4. ensure, that /src/main.clj has enabled remote-node
3. Start your repl

### Prepare ide-vm
1. install xubuntu14.04.03
2. login with your initial user
3. sudo apt-get update
4. sudo apt-get install opensssh-server
6. scp ~/.ssh/id_rsa [your execution folder]

### install ide
1. launch main ns in repl
2. (-main "init")
3. (-main "install")
4. (-main "configure")

### watch log for debug reasons
1. less logs/pallet.log

## Explanation of Configuration
...


# License
Licensed under Apache2.0
