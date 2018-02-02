### The Schema is:
```clojure
(def User                           
  {:password Secret,
   :name Str,
   (optional-key :gpg) {:gpg-passphrase Secret
                        :gpg-public-key Secret
                        :gpg-private-key Secret}
   (optional-key :ssh) {:ssh-private-key Secret
                        :ssh-public-key Secret}})
```

it's located in:
```
dda.pallet.dda-managed-vm.domain
```
and it's used in:
```
dda-managed-vm
dda-managed-ide
```
