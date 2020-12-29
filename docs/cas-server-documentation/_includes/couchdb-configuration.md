### CouchDb Configuration

The following options are shared and apply when CAS is configured to integrate with CouchDb (i.e ticket registry, etc):

```properties
# {{ include.configKey }}.couch-db.url=http://localhost:5984
# {{ include.configKey }}.couch-db.username=
# {{ include.configKey }}.couch-db.password=
# {{ include.configKey }}.couch-db.socket-timeout=10000
# {{ include.configKey }}.couch-db.connection-timeout=1000
# {{ include.configKey }}.couch-db.drop-collection=false
# {{ include.configKey }}.couch-db.max-connections=20
# {{ include.configKey }}.couch-db.enable-ssl=
# {{ include.configKey }}.couch-db.relaxed-ssl-settings=
# {{ include.configKey }}.couch-db.caching=false
# {{ include.configKey }}.couch-db.max-cache-entries=1000
# {{ include.configKey }}.couch-db.max-object-size-bytes=8192
# {{ include.configKey }}.couch-db.use-expect-continue=true
# {{ include.configKey }}.couch-db.cleanup-idle-connections=true
# {{ include.configKey }}.couch-db.create-if-not-exists=true
# {{ include.configKey }}.couch-db.proxy-host=
# {{ include.configKey }}.couch-db.proxy-port=-1

# Defaults are based on the feature name.
# {{ include.configKey }}.couch-db.db-name=

# For the few features that can't have update conflicts automatically resolved.
# {{ include.configKey }}.couch-db.retries=5

# Depending on the feature at hand, CAS may perform some actions asynchronously.
# {{ include.configKey }}.couch-db.asynchronous=true
```
