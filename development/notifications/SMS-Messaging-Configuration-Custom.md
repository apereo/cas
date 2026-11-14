# Custom SMS Messaging

Source: https://apereo.github.io/cas/development/notifications/SMS-Messaging-Configuration-Custom.html

Send text messages using your own custom implementation.

```java
@Bean
public SmsSender smsSender() {
    ...
}
```

To learn more about how to extend and customize the CAS configuration, please [review this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html).
