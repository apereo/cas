---
layout: default
title: Localization - User Interface Customization - CAS
category: User Interface
---

{% include variables.html %}

# Localization

The CAS Web application includes a number of localized message files:

- English (US)
- Spanish
- French
- Russian
- Dutch (Netherlands)
- Swedish (Svenskt)
- Italian (Italiano)
- Urdu
- Chinese (Simplified)
- German (Deutsch)
- Japanese
- Croatian
- Czech
- Slovenian
- Polish
- Portuguese (Brazil)
- Turkish
- Farsi
- Arabic

In order to "invoke" a specific language for the UI, the `/login` endpoint may be passed a `locale` parameter as such:

```bash
https://cas.server.org/login?locale=it
```

<div class="alert alert-warning"><strong>Usage Warning!</strong><p>Note that not all languages are 
complete and accurate across CAS server releases as translations are entirely dependent upon community contributions.
For an accurate and complete list of localized messages, always refer to the English language bundle.</p></div>

## Configuration

All message bundles are marked under `messages_xx.properties` files at `src/main/resources`.
The default language bundle is for the
English language and is thus called `messages.properties`. If there are any custom 
messages that need to be presented into views,
they may also be formatted under `custom_messages.properties` files.

In the event that the code is not found in the activated resource bundle, the code itself will be used verbatim.

### Localization

{% include_cached casproperties.html properties="cas.locale." %}

### Message Bundles

{% include_cached casproperties.html properties="cas.message-bundle." %}

## Per Service

Language locale may also be determined on a per-service basis:

```json
{
  "@class" : "org.apereo.cas.services.RegexRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "locale" : "de",
  "id" : 1
}
```

Locale names can use the [Spring Expression Language](../configuration/Configuration-Spring-Expressions.html) syntax.
