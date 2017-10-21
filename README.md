# dda-managed-vm
[![Clojars Project](https://img.shields.io/clojars/v/dda/dda-managed-vm.svg)](https://clojars.org/dda/dda-managed-vm)
[![Build Status](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm.svg?branch=master)](https://travis-ci.org/DomainDrivenArchitecture/dda-managed-vm)

Requirements can be found at https://dda.gitbooks.io/domaindrivenarchitecture/content/en/80_config_management/30_requirements/index.html

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

### gpg key & ssh key
As part of dda-user-crate your gpg- and ssh key can be installed.

### More Software
* Java JRE 1.8
* LibreOffice & SpellChecking


## Configure & Install
Use dda-managed-vm on order to install you personal vm.

### Prepare vm
1. install xubuntu16.04.02
2. login with your initial user
```
sudo apt-get update
sudo apt-get upgrade
sudo apt-get install openssh-server openjdk-7-jre-headless
```

### Configure your vm
1. Get your own clone
  1. git clone
2. Adjust your own configuration
  1. cd dda-managed-vm
  2. Adjust your configuration at user-config.edn

### install vm remote
1. Start your repl
2. (in-ns 'dda.pallet.dda-managed-vm.app.instantiate-existing)
3. (apply-install)

### watch log for debug reasons
1. less logs/pallet.log


# License
Licensed under Apache2.0
