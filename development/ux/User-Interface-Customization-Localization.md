# Localization

Source: https://apereo.github.io/cas/development/ux/User-Interface-Customization-Localization.html

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

In order to “invoke” a specific language for the UI, the `/login` endpoint may be passed a `locale` parameter as such:

```bash
https://cas.server.org/login?locale=it
```

> **Usage Warning!**
>
> Note that not all languages are complete and accurate across CAS server releases as translations are entirely dependent upon community contributions. For an accurate and complete list of localized messages, always refer to the English language bundle.

## Configuration

All message bundles are marked under `messages_xx.properties` files at `src/main/resources`. The default language bundle is for the English language and is thus called `messages.properties`. If there are any custom messages that need to be presented into views, they may also be formatted under `custom_messages.properties` files.

In the event that the code is not found in the activated resource bundle, the code itself will be used verbatim.

### Localization

The following settings and properties are available from the CAS configuration catalog:

- `cas.locale.cookie.domain` (Optional; type `String`): Cookie domain. Specifies the domain within which this cookie should be presented. The form of the domain name is specified by RFC 2965. A domain name begins with a dot (.foo.com) and means that the cookie is visible to servers in a specified Domain Name System (DNS) zone (for example, www.foo.com, but not a.b.foo.com). By default, cookies are only returned to the server that sent them.
- `cas.locale.cookie.http-only` (Optional; type `Boolean`; default `true`): true if this cookie contains the HttpOnly attribute. This means that the cookie should not be accessible to scripting engines, like javascript.
- `cas.locale.cookie.max-age` (Optional; type `String`; default `-1`): The maximum age of the cookie, specified in seconds. By default, `-1` indicating the cookie will persist until browser shutdown. A positive value indicates that the cookie will expire after that many seconds have passed. Note that the value is the maximum age when the cookie will expire, not the cookie's current age. A negative value means that the cookie is not stored persistently and will be deleted when the Web browser exits. A zero value causes the cookie to be deleted.
- `cas.locale.cookie.name` (Optional; type `String`): Cookie name. Constructs a cookie with a specified name and value. The name must conform to RFC 2965. That means it can contain only ASCII alphanumeric characters and cannot contain commas, semicolons, or white space or begin with a `$` character. The cookie's name cannot be changed after creation. By default, cookies are created according to the RFC 2965 cookie specification. Cookie names are automatically calculated assigned by CAS at runtime, and there is usually no need to customize the name or assign it a different value unless a special use case warrants the change.
- `cas.locale.cookie.path` (Optional; type `String`): Cookie path. Specifies a path for the cookie to which the client should return the cookie. The cookie is visible to all the pages in the directory you specify, and all the pages in that directory's subdirectories. A cookie's path must include the servlet that set the cookie, for example, /catalog, which makes the cookie visible to all directories on the server under /catalog. Consult RFC 2965 (available on the Internet) for more information on setting path names for cookies.
- `cas.locale.cookie.same-site-policy` (Optional; type `String`): If a cookie is only intended to be accessed in a first party context, the developer has the option to apply one of settings `SameSite=Lax` or `SameSite=Strict` or `SameSite=None` to prevent external access. To safeguard more websites and their users, the new secure-by-default model assumes all cookies should be protected from external access unless otherwise specified. Developers must use a new cookie setting, `SameSite=None`, to designate cookies for cross-site access. When the `SameSite=None` attribute is present, an additional `Secure` attribute is used so cross-site cookies can only be accessed over HTTPS connections. Accepted values are: - `Lax` - `Strict` - `None` - `Off`: Disable the generation of the SameSite cookie attribute altogether. - Path to a Groovy script that is able to generate the SameSite cookie attribute dynamically. - Fully qualified name of a class that implements `org.apereo.cas.web.cookie.CookieSameSitePolicy`
- `cas.locale.cookie.secure` (Optional; type `Boolean`; default `true`): True if sending this cookie should be restricted to a secure protocol, or false if the it can be sent using any protocol.
- `cas.locale.default-value` (Optional; type `String`; default `en`): Default locale.
- `cas.locale.force-default-locale` (Optional; type `Boolean`; default `false`): When set to true, locale resolution via request parameters and such is ignored and the locale default value is always enforced.
- `cas.locale.param-name` (Optional; type `String`; default `locale`): Parameter name to use when switching locales.

### Message Bundles

The following settings and properties are available from the CAS configuration catalog:

- `cas.message-bundle.base-names` (Optional; type `List<String>`): A list of strings representing base names for this message bundle. Set an array of basenames, each following the basic ResourceBundle convention of not specifying file extension or language codes. The resource location format is up to the specific MessageSource implementation. Regular and XMl properties files are supported: e.g. "messages" will find a "messages.properties", "messages_en.properties" etc arrangement as well as "messages.xml", "messages_en.xml" etc. The associated resource bundles will be checked sequentially when resolving a message code. Note that message definitions in a previous resource bundle will override ones in a later bundle, due to the sequential lookup.
- `cas.message-bundle.cache-seconds` (Optional; type `String`; default `PT180S`): Cache size.
- `cas.message-bundle.common-names` (Optional; type `List<String>`): A list of strings representing common names for this message bundle. Specify locale-independent common messages, with the message code as key and the full message String (may contain argument placeholders) as value. Entries in last common names override first values (as opposed to baseNames used in message bundles).
- `cas.message-bundle.encoding` (Optional; type `String`): Message bundle character encoding.
- `cas.message-bundle.fallback-system-locale` (Optional; type `Boolean`; default `false`): Flag that controls whether to fallback to the default system locale if no locale is specified explicitly. Set whether to fall back to the system Locale if no files for a specific Locale have been found. If this is turned off, the only fallback will be the default file (e.g. "messages.properties" for basename "messages"). Falling back to the system Locale is the default behavior of `ResourceBundle`. However, this is often not desirable in an application server environment, where the system Locale is not relevant to the application at all: set this flag to `false` in such a scenario.
- `cas.message-bundle.use-code-message` (Optional; type `Boolean`; default `true`): Flag that controls whether to use code message. Set whether to use the message code as default message instead of throwing a `NoSuchMessageException`. Useful for development and debugging. Note: In case of a `MessageSourceResolvable` with multiple codes (like a FieldError) and a MessageSource that has a parent MessageSource, do not activate "useCodeAsDefaultMessage" in the parent: Else, you'll get the first code returned as message by the parent, without attempts to check further codes.

## Per Service

Language locale may also be determined on a per-service basis:

```json
{
  "@class" : "org.apereo.cas.services.CasRegisteredService",
  "serviceId" : "^https://www.example.org",
  "name" : "MyTheme",
  "locale" : "de",
  "id" : 1
}
```

Locale names can use the [Spring Expression Language](https://apereo.github.io/cas/development/configuration/Configuration-Spring-Expressions.html) syntax.
