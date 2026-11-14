# Graphical User Authentication

Source: https://apereo.github.io/cas/development/authentication/GUA-Authentication.html

Graphical user authentication, sometimes also known as ‘login images’ are a form of login verification (i.e. second factor) where a site presents the user with an image previously selected by the user at the time the account is created. It is an “account secret” tied to the username that should not be easily reproduced by a phishing campaign attempting to impersonate a legitimate website.

In practice, CAS prompts the user for only their username and responds with a page displaying what should be the user’s preselected image along with a password field to complete their authentication. The user in turn is to be trained to refuse submitting the rest of their login credentials to a site posing to be legitimate if CAS fails to present the correct image.

## Overview

Support is enabled by including the following module in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-gua"
}
```

### Resource

Please [see this guide](https://apereo.github.io/cas/development/authentication/GUA-Authentication-Storage-Resource.html).

### LDAP

Please [see this guide](https://apereo.github.io/cas/development/authentication/GUA-Authentication-Storage-LDAP.html).
