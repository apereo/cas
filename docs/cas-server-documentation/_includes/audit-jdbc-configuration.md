The following settings are available for JDBC Audit integrations:

```properties
# cas.authn.throttle.jdbc.audit-query=SELECT AUD_DATE FROM COM_AUDIT_TRAIL \
#   WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? \
#   AND AUD_ACTION = ? AND APPLIC_CD = ? \
#   AND AUD_DATE >= ? ORDER BY AUD_DATE DESC
```
