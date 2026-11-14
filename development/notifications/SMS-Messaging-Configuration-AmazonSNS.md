# Amazon SNS SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-AmazonSNS.html

Send SMS messages from CAS through Amazon Simple Notification Service.

To learn more, [visit this site](https://docs.aws.amazon.com/sns).

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-sms-aws-sns"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.sms-provider.sns.credential-access-key` (Required; type `String`): Use access-key provided by AWS to authenticate.
- `cas.sms-provider.sns.credential-secret-key` (Required; type `String`): Use secret key provided by AWS to authenticate.
- `cas.sms-provider.sns.endpoint` (Required; type `String`): AWS custom endpoint.
- `cas.sms-provider.sns.region` (Required; type `String`): AWS region used.
- `cas.sms-provider.sns.client-execution-timeout` (Optional; type `String`; default `PT10S`): Client execution timeout.
- `cas.sms-provider.sns.connection-timeout` (Optional; type `String`; default `PT5S`): Connection timeout.
- `cas.sms-provider.sns.local-address` (Optional; type `String`): Local address.
- `cas.sms-provider.sns.max-connections` (Optional; type `Integer`; default `10`): Maximum connections setting.
- `cas.sms-provider.sns.max-price` (Optional; type `String`): The maximum amount in USD that you are willing to spend to send the SMS message. Amazon SNS will not send the message if it determines that doing so would incur a cost that exceeds the maximum price. This attribute has no effect if your month-to-date SMS costs have already exceeded the limit set for the MonthlySpendLimit attribute, which you set by using the SetSMSAttributes request. If you are sending the message to an Amazon SNS topic, the maximum price applies to each message delivery to each phone number that is subscribed to the topic.
- `cas.sms-provider.sns.profile-name` (Optional; type `String`): Profile name to use.
- `cas.sms-provider.sns.profile-path` (Optional; type `String`): Profile path.
- `cas.sms-provider.sns.proxy-host` (Optional; type `String`): Optionally specifies the proxy host to connect through.
- `cas.sms-provider.sns.proxy-password` (Optional; type `String`): Optionally specifies the proxy password to connect through.
- `cas.sms-provider.sns.proxy-username` (Optional; type `String`): Optionally specifies the proxy username to connect through.
- `cas.sms-provider.sns.retry-mode` (Optional; type `String`; default `STANDARD`): Outline the requested retry mode. Accepted values are `STANDARD, LEGACY`.
- `cas.sms-provider.sns.sender-id` (Optional; type `String`): A custom ID that contains up to 11 alphanumeric characters, including at least one letter and no spaces. The sender ID is displayed as the message sender on the receiving device. For example, you can use your business brand to make the message source easier to recognize. Support for sender IDs varies by country and/or region. For example, messages delivered to U.S. phone numbers will not display the sender ID. If you do not specify a sender ID, the message will display a long code as the sender ID in supported countries and regions. For countries or regions that require an alphabetic sender ID, the message displays NOTICE as the sender ID.
- `cas.sms-provider.sns.sms-type` (Optional; type `String`; default `Transactional`): The type of message that you are sending. - Promotional - Noncritical messages, such as marketing messages. Amazon SNS optimizes the message delivery to incur the lowest cost. - Transactional – Critical messages that support customer transactions, such as one-time passcodes for multi-factor authentication. Amazon SNS optimizes the message delivery to achieve the highest reliability.
- `cas.sms-provider.sns.socket-timeout` (Optional; type `String`; default `PT5S`): Socket timeout.
- `cas.sms-provider.sns.use-reaper` (Optional; type `Boolean`; default `false`): Flag that indicates whether to use reaper.
