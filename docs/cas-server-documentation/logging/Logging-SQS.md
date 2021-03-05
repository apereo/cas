---
layout: default
title: CAS - SQS Logging Configuration
category: Logs & Audits
---

{% include variables.html %}

# AWS SQS Logging

Log data can be automatically routed to [AWS SQS](https://aws.amazon.com/sqs/). Support 
is enabled by including the following module in the overlay:

{% include casmodule.html group="org.apereo.cas" module="cas-server-support-logging-config-sqs" %}

With the above module, you may then declare a specific appender to communicate with AWS SQS:

```xml
<SQSAppender name="SQSAppender"
             region="us-west-1"
             credentialAccessKey="..."
             credentialSecretKey="..."
             queueName="CAS"
             endpoint="..."
             queueTags="tag1->value1,tag2->value2">
 <PatternLayout>
  <Pattern>%5p | %d{ISO8601}{UTC} | %t | %C | %M:%L | %m %ex %n</Pattern>
 </PatternLayout>
</SQSAppender>

...

<Logger name="org.apereo.cas" level="trace" additivity="false">
    <AppenderRef ref="SQSAppender"/>
</Logger>
```
 
The `endpoint` setting is optional. If the SQS queue indicated by `queueName` does not exist, it will be automatically
created by CAS when the appender is initialized and started.

AWS credentials are fetched from the following sources automatically, where relevant and made possible via CAS configuration:

1. EC2 instance metadata linked to the IAM role.
2. External properties file that contains `accessKey` and `secretKey` as property keys.
3. AWS profile path and profile name.
4. System properties that include `aws.accessKeyId`, `aws.secretKey` and `aws.sessionToken`
5. Environment variables that include `AWS_ACCESS_KEY_ID`, `AWS_SECRET_KEY` and `AWS_SESSION_TOKEN`.
6. Properties file on the classpath as `awscredentials.properties` that contains `accessKey` and `secretKey` as property keys.
7. Static credentials for access key and secret provided directly by the configuration at hand (logging, etc).
