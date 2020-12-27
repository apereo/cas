### Git Configuration

The following options related to Git integration support in CAS when it attempts to connect and pull/push changes:

```properties
# {{ include.configKey }}.git.repository-url=https://github.com/repository
# {{ include.configKey }}.git.branches-to-clone=master
# {{ include.configKey }}.git.active-branch=master
# {{ include.configKey }}.git.sign-commits=false
# {{ include.configKey }}.git.username=
# {{ include.configKey }}.git.password=
# {{ include.configKey }}.git.clone-directory.location=file:/tmp/cas-service-registry
# {{ include.configKey }}.git.push-changes=false
# {{ include.configKey }}.git.private-key-passphrase=
# {{ include.configKey }}.git.private-key.location=file:/tmp/privkey.pem
# {{ include.configKey }}.git.ssh-session-password=
# {{ include.configKey }}.git.timeout=PT10S
# {{ include.configKey }}.git.strict-host-key-checking=true
# {{ include.configKey }}.git.clear-existing-identities=false
```
