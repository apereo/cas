The following settings are available for JDBC Audit integrations:

```properties
# cas.authn.throttle.jdbc.audit-query=SELECT AUD_DATE FROM COM_AUDIT_TRAIL \
#   WHERE AUD_CLIENT_IP = ? AND AUD_USER = ? \
#   AND AUD_ACTION = ? AND APPLIC_CD = ? \
#   AND AUD_DATE >= ? ORDER BY AUD_DATE DESC

# cas.audit.jdbc.asynchronous=true
# cas.audit.jdbc.max-age-days=180
# cas.audit.jdbc.column-length=100
# cas.audit.jdbc.select-sql-query-template=
# cas.audit.jdbc.date-formatter-pattern=
```
