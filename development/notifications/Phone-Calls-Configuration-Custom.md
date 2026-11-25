# Phone Calls - Custom Operators

Source: https://apereo.github.io/cas/development/notifications/Phone-Calls-Configuration-Custom.html

Make phone calls through an operator CAS does not support by implementing `PhoneCallOperator` and registering it with CAS.

You may define your own phone operator that would be tasked to make calls, etc using the following bean definition and by implementing `PhoneCallOperator`:

```java
@Bean
public PhoneCallOperator phoneCallOperator() {
    return new MyPhoneCallOperator();
}
```

[See this guide](https://apereo.github.io/cas/development/configuration/Configuration-Management-Extensions.html) to learn more about how to register configurations into the CAS runtime.
