<!-- fragment:keep -->

<p/>

Certain aspects of CAS such as authentication handling support configuration of
password encoding. Most options are based on Spring Security's [support for password encoding](https://docs.spring.io/spring-security/site/docs/current/reference/html5/).


The following options are supported:

| Type                    | Description
|-------------------------|----------------------------------------------------------------------------------------------------
| `NONE`                  | No password encoding (i.e. plain-text) takes place.
| `DEFAULT`               | Use the `DefaultPasswordEncoder` of CAS. For message-digest algorithms via `characterEncoding` and `encodingAlgorithm`.
| `BCRYPT`                | Use the `BCryptPasswordEncoder` based on the `strength` provided and an optional `secret`.
| `SCRYPT`                | Use the `SCryptPasswordEncoder`.
| `PBKDF2`                | Use the `Pbkdf2PasswordEncoder` based on the `strength` provided and an optional `secret`.
| `STANDARD`              | Use the `StandardPasswordEncoder` based on the `secret` provided.
| `SSHA`                  | Use the `LdapShaPasswordEncoder` supports Ldap SHA and SSHA (salted-SHA). The values are base-64 encoded and have the label `{SHA}` (or `{SSHA}`) prepended to the encoded hash.
| `GLIBC_CRYPT`           | Use the `GlibcCryptPasswordEncoder` based on the [`encoding-algorithm`](https://commons.apache.org/proper/commons-codec/archives/1.10/apidocs/org/apache/commons/codec/digest/Crypt.html), `strength` provided and an optional `secret`.
| `org.example.MyEncoder` | An implementation of `PasswordEncoder` of your own choosing.
| `file:///path/to/script.groovy` | Path to a Groovy script charged with handling password encoding operations.

In cases where you plan to design your own password encoder or write scripts to do so,
you may also need to ensure the overlay has access to `org.springframework.security:spring-security-core` 
at runtime. Make sure the artifact is marked as `provided` or `compileOnly` to avoid conflicts.

If you need to design your own password encoding scheme where the type is specified as a fully qualified Java class name, the structure of the class would be similar to the following:

```java
package org.example.cas;

import org.springframework.security.crypto.codec.*;
import org.springframework.security.crypto.password.*;

public class MyEncoder extends AbstractPasswordEncoder {
    @Override
    protected byte[] encode(CharSequence rawPassword, byte[] salt) {
        return ...
    }
}
```

If you need to design your own password encoding scheme where the type is 
specified as a path to a Groovy script, the structure of the script would be similar to the following:

```groovy
import java.util.*

byte[] run(final Object... args) {
    def rawPassword = args[0]
    def generatedSalt = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

    logger.debug("Encoding password...")
    return ...
}

Boolean matches(final Object... args) {
    def rawPassword = args[0]
    def encodedPassword = args[1]
    def logger = args[2]
    def casApplicationContext = args[3]

   logger.debug("Does match or not ?");
   return ...
```
