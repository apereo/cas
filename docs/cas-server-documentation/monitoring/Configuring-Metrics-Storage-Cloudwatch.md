---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Amazon Cloudwatch Storage - CAS Metrics

[AWS CloudWatch](https://aws.amazon.com/cloudwatch/) is a monitoring and observability service in the AWS cloud platform. One of 
its main features is collecting metrics and storing the metrics in a time-series database. It is a dimensional time-series service 
in the AWS cloud platform. It provides the following features:

- Collecting and monitoring logs.
- Storing metrics from AWS resources, and applications running in AWS or outside AWS.
- Providing system-wide visualization with graphs and statistics.
- Creating alarms that watch a single or multiple CloudWatch metrics and perform some actions based on the value of the metric.

Support is enabled by including the following module in the overlay:

{% include_cached casmodule.html group="org.apereo.cas" module="cas-server-support-aws-cloudwatch" %}

## Configuration 

The default configuration provides for the following:

- Metrics are asynchronously sent to AWS CloudWatch every `30` seconds.
- The predefined AWS region is `us-east-1`.
- The predefined AWS CloudWatch namespace is `apereo-cas`.
- Connection and read timeouts are predefined to be `5` seconds.
