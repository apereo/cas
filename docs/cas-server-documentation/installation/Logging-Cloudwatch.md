---
layout: default
title: CAS - Cloudwatch Logging Configuration
---

# Cloudwatch Logging

Log data can be automatically routed to [AWS CloudWatch](https://aws.amazon.com/cloudwatch/). Support is enabled by including the following module in the overlay:

```xml
<dependency>
     <groupId>org.apereo.cas</groupId>
     <artifactId>cas-server-core-logging-config-cloudwatch</artifactId>
     <version>${cas.version}</version>
</dependency>
```

With the above module, you may then declare a specific appender to communicate with AWS CloudWatch:

```xml
<CloudWatchAppender name="cloudWatch"
                    awsLogGroupName="LogGroupName"
                    awsLogStreamName="LogStreamName"
                    awsLogRegionName="us-west-1"
                    credentialAccessKey="..."
                    credentialSecretKey="..."
                    awsLogStreamFlushPeriodInSeconds="5">
    <PatternLayout>
        <Pattern>%5p | %d{ISO8601}{UTC} | %t | %C | %M:%L | %m %ex %n</Pattern>
    </PatternLayout>
</CloudWatchAppender>
...
<AsyncLogger name="org.apereo" additivity="true" level="debug">
    <appender-ref ref="cloudWatch" />
</AsyncLogger>
```

The AWS credentials for access key, secret key and region, if left undefined, may also be retrieved from
system properties via `AWS_ACCESS_KEY`, `AWS_SECRET_KEY` and `AWS_REGION_NAME`.
The group name as well as the stream name are automatically created by CAS, if they are not already found.
