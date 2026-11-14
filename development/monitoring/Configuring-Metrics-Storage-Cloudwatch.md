# Amazon Cloudwatch Storage - CAS Metrics

Source: https://apereo.github.io/cas/development/monitoring/Configuring-Metrics-Storage-Cloudwatch.html

Export CAS metrics to Amazon CloudWatch for monitoring in AWS.

[AWS CloudWatch](https://aws.amazon.com/cloudwatch/) is a monitoring and observability service in the AWS cloud platform. One of its main features is collecting metrics and storing the metrics in a time-series database. It is a dimensional time-series service in the AWS cloud platform. It provides the following features:

- Collecting and monitoring logs.
- Storing metrics from AWS resources, and applications running in AWS or outside AWS.
- Providing system-wide visualization with graphs and statistics.
- Creating alarms that watch a single or multiple CloudWatch metrics and perform some actions based on the value of the metric.

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-aws-cloudwatch"
}
```

## Configuration

The default configuration provides for the following:

- Metrics are asynchronously sent to AWS CloudWatch every `30` seconds.
- The predefined AWS region is `us-east-1`.
- The predefined AWS CloudWatch namespace is `apereo-cas`.
- Connection and read timeouts are predefined to be `5` seconds.
