### DynamoDb Configuration

The following options related to DynamoDb support in CAS apply equally to a number of CAS components (ticket registries, etc):

```properties
# {{ include.configKey }}.dynamo-db.drop-tables-on-startup=false
# {{ include.configKey }}.dynamo-db.prevent-table-creation-on-startup=false
# {{ include.configKey }}.dynamo-db.local-instance=false
```

{% include {{ version }}/aws-configuration.md configKey="{{ include.configKey }}.dynamo-db" %}
