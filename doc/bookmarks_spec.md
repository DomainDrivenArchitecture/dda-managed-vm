### The Schema is:
```clojure
(def Bookmarks                      ; see dda-managed-vm
  [{(optional-key :childs) [(recursive
                           (var
                            dda.pallet.dda-managed-vm.infra.browser/Folder))],
  :name Str,
  (optional-key :links) [[(one Str "url") (one Str "name")]]}])
```

it's located in:
```
dda.pallet.dda-managed-vm.infra.browser
```
and it's used in:
```
dda-managed-vm
dda-managed-ide
```
