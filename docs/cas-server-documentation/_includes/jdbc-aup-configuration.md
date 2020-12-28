If AUP is controlled via JDBC, decide how choices should be remembered back inside the database instance.

```properties
# cas.acceptable-usage-policy.jdbc.table-name=usage_policies_table
# cas.acceptable-usage-policy.jdbc.aup-column=
# cas.acceptable-usage-policy.jdbc.principal-id-column=username
# cas.acceptable-usage-policy.jdbc.principal-id-attribute=
# cas.acceptable-usage-policy.jdbc.sql-update=UPDATE %s SET %s=true WHERE %s=?
```
