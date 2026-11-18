# Notifications - Slack

Source: https://apereo.github.io/cas/development/notifications/Notifications-Configuration-Slack.html

Send notifications to users as Slack messages.

Support is enabled via the relevant modules using the following module:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-notifications-slack"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `cas.slack-messaging.api-token` (Required; type `String`): To call a Slack Web API method to post messages, CAS needs to be initialized with a Slack API token. A token usually begins with `xoxb-` (bot token) or `xoxp-` (user token). You get them from each workspace that an app has been installed.
- `cas.slack-messaging.username-attribute` (Optional; type `String`): The name of the user attribute that would then be used as the slack username. If the attribute is left blank, the default principal identifier is used. Note that in either case the final value is prefixed with `@`, but only if the prefix does not already exist. Multivalued attributes are supported.
