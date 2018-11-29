The schema for the vm configuration is:
```clojure
(def Secret                           ; see dda-pallet-commons
  (either
    {:plain Str}                      ;   as plain text
    {:password-store-single Str}      ;   as password store key wo linebreaks & whitespaces
    {:password-store-record           ;   as password store entry containing login (record :login)
      {:path Str,                     ;      and password (no field or :password)
       :element (enum :password :login)}}
    {:password-store-multi Str}       ;   as password store key with linebreaks
    {:pallet-secret {:key-id Str,
                    :service-path [Keyword],
                    :record-element (enum :secret :account)}})

(def GitCredentials
  {(enum :gitblit :github)
   {:user s/Str
    (optional-key :password) Secret}})

(def User
  {:password Secret,
   :name Str,
   (optional-key :email) Str                    ; email for git config
   (optional-key :git-credentials)              ; credentials for git repositories
   git/GitCredentials
   (s/optional-key :git-signing-key) s/Str      ; key used for sign git commits or tags
   (optional-key :desktop-wiki) [Str]           ; install zim desktop-wiki, Str to describe
                                                ; used autosync git repositories
   (optional-key :credentials) [Str]            ; install passwordstore or gopass, Str to describe
                                                ; used git repositories
   (optional-key :gpg) {:gpg-passphrase Secret
                        :gpg-public-key Secret
                        :gpg-private-key Secret}
   (optional-key :ssh) {:ssh-private-key Secret
                        :ssh-public-key Secret}})

(def Bookmarks                        ; see dda-managed-vm
  [{(optional-key :childs) [(recursive
                           (var
                            dda.pallet.dda-managed-vm.infra.browser/Folder))],
  :name Str,
  (optional-key :links) [[(one Str "url") (one Str "name")]]}])


(def DdaVmDomainConfig
   {:target-type
    (s/enum :virtualbox               ; vbox-guest-utils and vm-specials like swappiness and random generator utils.
            :remote-aws               ; vm-specials and tight-vnc
            :plain)                   ; for non vm-installations
    :usage-type
    (s/enum :desktop-minimal          ; only some analysis tools are installed.
            :desktop-base             ; in addition java-11 anfd git
            :desktop-ide             ; in addition key-mgm, credential-mgm, zim, java-8 and libreoffice is installed.
            :desktop-office)         ; in addition fakturama and audio is installed.
    (optional-key :bookmarks)
    Bookmarks,                        ; initial bookmarks
    :user User                        ; user to create with his credentials
  })
```

For `Secret` you can find more adapters in dda-palet-commons.
