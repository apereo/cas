### Email Configuration

The following options are shared and apply when CAS is configured to send 
email notifications:

```properties
# {{ include.configKey }}.mail.from=
# {{ include.configKey }}.mail.text=
# {{ include.configKey }}.mail.subject=
# {{ include.configKey }}.mail.cc=
# {{ include.configKey }}.mail.bcc=
# {{ include.configKey }}.mail.reply-to=
# {{ include.configKey }}.mail.validate-addresses=false
# {{ include.configKey }}.mail.html=false

# {{ include.configKey }}.mail.attribute-name=mail
```

The following settings may also need to be defined to describe the mail server settings:

```properties
# spring.mail.host=
# spring.mail.port=
# spring.mail.username=
# spring.mail.password=
# spring.mail.properties.mail.smtp.auth=true
# spring.mail.properties.mail.smtp.starttls.enable=true
```
