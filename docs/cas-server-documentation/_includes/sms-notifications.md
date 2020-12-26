### SMS Configuration

The following options are shared and apply when CAS is configured to send 
SMS notifications:

```properties
# {{ include.configKey }}.sms.from=
# {{ include.configKey }}.sms.text=
# {{ include.configKey }}.sms.attribute-name=phone
```

You will also need to ensure a provider is defined that is able to send SMS messages. 
