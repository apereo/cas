# JSON Authentication Interrupt

Source: https://apereo.github.io/cas/development/webflow/Webflow-Customization-Interrupt-JSON.html

The following settings and properties are available from the CAS configuration catalog:

- `cas.interrupt.json.location` (Required; type `Resource`): The location of the resource. Resources can be URLs, or files found either on the classpath or outside somewhere in the file system. In the event the configured resource is a Groovy script, especially if the script is set to reload on changes, you may need to adjust the total number of `inotify` instances. On Linux, you may need to add the following line to `/etc/sysctl.conf`: `fs.inotify.max_user_instances = 256`. You can check the current value via `cat /proc/sys/fs/inotify/max_user_instances`. In situations and scenarios where CAS is able to automatically watch the underlying resource for changes and detect updates and modifications dynamically, you may be able to specify the following setting as either an environment variable or system property with a value of `false` to disable the resource watcher: `org.apereo.cas.util.io.PathWatcherService`.

This strategy reaches out to a static JSON resource that contains a map of usernames linked to various interrupt policies. This option is most useful during development, testing and demos.

The resource is loaded once at startup. A file-based resource is also watched for changes and reloaded automatically; if an updated file cannot be read or parsed, the previously loaded policies remain in effect.

```json
{
  "casuser" : {
    "message" : "Announcement message <strong>goes here</strong>.",
    "messageCode" : "interrupt.message.body",
    "links" : {
      "Go to Location1" : "https://www.location1.com",
      "Go to Location2" : "https://www.location2.com"
    },
    "block" : false,
    "ssoEnabled" : false,
    "interrupt" : true,
    "autoRedirect" : false,
    "autoRedirectAfterSeconds" : -1,
    "data" : {
      "field1" : [ "value1", "value2" ],
      "field2" : [ "value3", "value4" ]
    }
  }
}
```

## Interrupt Payload

Each interrupt strategy is ultimately tasked to produce a response that contains the following settings:

| Field | Description |
| --- | --- |
| `message` | Announcement message to display on the screen. |
| `messageCode` | The language bundle key that points to an actual message to be displayed on the screen. Should be used as an alternative to the `message` field. |
| `links` | A map of links to display on the screen where key is the link text and value is the destination. |
| `interrupt` | `true/false` to indicate whether CAS should interrupt the authentication flow. |
| `block` | `true/false` to indicate whether CAS should block the authentication flow altogether. |
| `ssoEnabled` | `true/false` to indicate whether CAS should permit the authentication but not establish SSO. |
| `autoRedirect` | `true/false` to indicate whether CAS should auto-redirect to the first provided link. |
| `autoRedirectAfterSeconds` | Indicate whether CAS should auto-redirect after the configured number of seconds. The default is `-1`, meaning delayed redirect functionality should not be executed. |
| `data` | A map of key-value pairs to pass along to the UI. |
