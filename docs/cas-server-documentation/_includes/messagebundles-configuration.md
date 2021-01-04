The baseNames are message bundle base names representing files that either 
end in `.properties` or `_xx.properties` where xx is a country locale code. 

The `common-names` are not actually message bundles but they are properties files that 
are merged together and contain keys that are only used if they are not found in 
the message bundles. Keys from the later files in the list will be preferred over keys from the earlier files.

```properties
# cas.message-bundle.encoding=UTF-8
# cas.message-bundle.fallback-system-locale=false
# cas.message-bundle.cache-seconds=180
# cas.message-bundle.use-code-message=true
# cas.message-bundle.base-names=classpath:custom_messages,classpath:messages
# cas.message-bundle.common-names=classpath:/common_messages.properties,file:/etc/cas/config/common_messages.properties
```
