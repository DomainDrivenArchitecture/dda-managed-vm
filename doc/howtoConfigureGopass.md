# After installation Setup for Gopass
1. copy main/resources/gopass.yml.templ to .config/gopass/config.yml
2. set your password-store-folder-path in `path: `
3. set your gpg-key-id in `.gpg-id:`
4. `gopass mounts add jem /home/jem/repo/credential-store/jem-pass`
5. `sudo ln -s /usr/local/bin/gopass /usr/local/bin/pass`
