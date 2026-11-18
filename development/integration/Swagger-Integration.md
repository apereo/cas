# Overview

Source: https://apereo.github.io/cas/development/integration/Swagger-Integration.html

CAS takes advantages of [Swagger](https://swagger.io/) to produce API documentation automatically. The generated documentation supports all CAS endpoints and REST APIs provided they are made available to the runtime application context and are present in the overlay.

Support is enabled by including the following dependency in the overlay:

```groovy
dependencies {
    /*
        The following platform references are included automatically and are listed for reference only.

        implementation enforcedPlatform("org.apereo.cas:cas-server-support-bom:${project.'cas.version'}")
        implementation platform(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
        
    */
    implementation "org.apereo.cas:cas-server-support-swagger"
}
```

The following settings and properties are available from the CAS configuration catalog:

- `springdoc.allowed-locales` (Third party; type `List<String>`): Allowed locales for i18n.
- `springdoc.api-docs.enabled` (Third party; type `Boolean`; default `false`): Whether to generate and serve an OpenAPI document.
- `springdoc.api-docs.groups.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.api-docs.path` (Third party; type `String`): Path to the generated OpenAPI documentation. For a yaml file, append ".yaml" to the path.
- `springdoc.api-docs.resolve-extensions-properties` (Third party; type `Boolean`; default `false`): The Resolve extensions properties.
- `springdoc.api-docs.resolve-schema-properties` (Third party; type `Boolean`; default `false`): The Resolve schema properties.
- `springdoc.api-docs.version` (Third party; type `SpringDocConfigProperties.ApiDocs.OpenApiVersion`; default `openapi-3-1`): The OpenAPI version.
- `springdoc.auto-tag-classes` (Third party; type `Boolean`; default `true`): The Auto tag classes.
- `springdoc.cache.disabled` (Third party; type `Boolean`; default `false`): The Disabled.
- `springdoc.cache.max-entries` (Third party; type `Integer`; default `100`): The maximum number of OpenAPI descriptions to keep in the cache. One entry is cached per requested locale, so this bounds the memory an untrusted caller can make the cache consume by varying the `Accept-Language` header. The least recently used entry is evicted once the limit is reached. A non-positive value falls back to the default of `#DEFAULT_MAX_ENTRIES` rather than meaning "unlimited". Narrow it further with `springdoc.allowed-locales`.
- `springdoc.consumes-to-match` (Third party; type `List<String>`): The Consumes to match.
- `springdoc.default-consumes-media-type` (Third party; type `String`): The Default consumes media type.
- `springdoc.default-flat-param-object` (Third party; type `Boolean`; default `false`): The param default flatten
- `springdoc.default-produces-media-type` (Third party; type `String`): The Default produces media type.
- `springdoc.default-support-form-data` (Third party; type `Boolean`; default `false`): convert query param to form data when consumes is multipart/form-data
- `springdoc.disable-i18n` (Third party; type `Boolean`; default `false`): The Disable i18n.
- `springdoc.enable-data-rest` (Third party; type `Boolean`; default `true`): The Enable hateoas.
- `springdoc.enable-default-api-docs` (Third party; type `Boolean`; default `true`): The Enable default api docs.
- `springdoc.enable-extra-schemas` (Third party; type `Boolean`; default `false`): The Enable extra schemas resolution.
- `springdoc.enable-groovy` (Third party; type `Boolean`; default `true`): The Enable groovy.
- `springdoc.enable-hateoas` (Third party; type `Boolean`; default `true`): The Enable hateoas.
- `springdoc.enable-javadoc` (Third party; type `Boolean`; default `true`): The Enable javadoc.
- `springdoc.enable-kotlin` (Third party; type `Boolean`; default `true`): The Enable kotlin.
- `springdoc.enable-spring-security` (Third party; type `Boolean`; default `true`): The Enable spring security.
- `springdoc.explicit-object-schema` (Third party; type `Boolean`; default `false`): Set explicit-object-schema to true to always include type: object in the schema, or to false to omit type: object.
- `springdoc.group-configs` (Third party; type `Set<SpringDocConfigProperties.GroupConfig>`): The Group configs.
- `springdoc.headers-to-match` (Third party; type `List<String>`): The Headers to match.
- `springdoc.login-endpoint.password-example` (Third party; type `String`): The example value for the password field of the login request body. Only applied when `springdoc.show-login-endpoint=true`.
- `springdoc.login-endpoint.username-example` (Third party; type `String`): The example value for the username field of the login request body. Only applied when `springdoc.show-login-endpoint=true`.
- `springdoc.model-and-view-allowed` (Third party; type `Boolean`; default `false`): The Model and view allowed.
- `springdoc.model-converters.deprecating-converter.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.model-converters.java-nullable-property-customizer.enabled` (Third party; type `Boolean`; default `false`): Mark the schema properties of Java models annotated with @Nullable as nullable.
- `springdoc.model-converters.kotlin-nullable-property-customizer.enabled` (Third party; type `Boolean`; default `true`): Mark the schema properties of nullable Kotlin types (Type?) as nullable.
- `springdoc.model-converters.pageable-converter.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.model-converters.polymorphic-converter.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.model-converters.polymorphic-converter.one-of-as-ref` (Third party; type `Boolean`; default `false`): Emit discovered polymorphic oneOf schemas as reusable component refs.
- `springdoc.model-converters.polymorphic-converter.one-of-ref-suffix` (Third party; type `String`; default `Variant`): Suffix appended to the base schema name when emitting oneOf unions as component refs.
- `springdoc.nullable-request-parameter-enabled` (Third party; type `Boolean`; default `false`): The Nullable request parameter enabled.
- `springdoc.open-api` (Third party; type `OpenAPI`): The Open api.
- `springdoc.override-with-generic-response` (Third party; type `Boolean`): The Override with generic response.
- `springdoc.packages-to-exclude` (Third party; type `List<String>`): The Packages to exclude.
- `springdoc.packages-to-scan` (Third party; type `List<String>`): The Packages to scan.
- `springdoc.paths-to-exclude` (Third party; type `List<String>`): The Paths to exclude.
- `springdoc.paths-to-match` (Third party; type `List<String>`): The Paths to match.
- `springdoc.pre-loading-enabled` (Third party; type `Boolean`; default `false`): Allow for pre-loading OpenAPI
- `springdoc.pre-loading-locales` (Third party; type `List<String>`): locale list to pre-loading
- `springdoc.produces-to-match` (Third party; type `List<String>`): The Produces to match.
- `springdoc.remove-broken-reference-definitions` (Third party; type `Boolean`; default `true`): The Remove broken reference definitions.
- `springdoc.show-actuator` (Third party; type `Boolean`; default `false`): The Show actuator.
- `springdoc.show-login-endpoint` (Third party; type `Boolean`; default `false`): The Show login endpoint.
- `springdoc.show-oauth2-endpoints` (Third party; type `Boolean`; default `false`): The Show oauth 2 endpoint.
- `springdoc.show-spring-cloud-functions` (Third party; type `Boolean`; default `true`): The Show spring cloud functions.
- `springdoc.sort-converter.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.swagger-ui.config-url` (Third party; type `String`): URL to fetch external configuration document from.
- `springdoc.swagger-ui.csrf.cookie-name` (Third party; type `String`): The Cookie name.
- `springdoc.swagger-ui.csrf.enabled` (Third party; type `Boolean`; default `false`): The Enabled.
- `springdoc.swagger-ui.csrf.header-name` (Third party; type `String`): The Header name.
- `springdoc.swagger-ui.csrf.local-storage-key` (Third party; type `String`): The Local storage key.
- `springdoc.swagger-ui.csrf.session-storage-key` (Third party; type `String`): The Session storage key.
- `springdoc.swagger-ui.csrf.use-local-storage` (Third party; type `Boolean`; default `false`): Use Local storage.
- `springdoc.swagger-ui.csrf.use-session-storage` (Third party; type `Boolean`; default `false`): Use Session storage.
- `springdoc.swagger-ui.deep-linking` (Third party; type `Boolean`): Enables or disables deep linking for tags and operations. @see [deep-linking.md](https://github.com/swagger-api/swagger-ui/blob/master/docs/usage/deep-linking.md)
- `springdoc.swagger-ui.default-model-expand-depth` (Third party; type `Integer`): The default expansion depth for the model on the model-example section.
- `springdoc.swagger-ui.default-model-rendering` (Third party; type `String`): Controls how the model is shown when the API is first rendered.
- `springdoc.swagger-ui.default-models-expand-depth` (Third party; type `Integer`): The default expansion depth for models (set to -1 completely hide the models).
- `springdoc.swagger-ui.disable-swagger-default-url` (Third party; type `Boolean`; default `false`): The Disable swagger default url.
- `springdoc.swagger-ui.display-operation-id` (Third party; type `Boolean`): Controls the display of operationId in operations list.
- `springdoc.swagger-ui.display-request-duration` (Third party; type `Boolean`): Controls the display of the request duration (in milliseconds) for Try-It-Out requests.
- `springdoc.swagger-ui.doc-expansion` (Third party; type `String`): Controls the default expansion setting for the operations and tags.
- `springdoc.swagger-ui.document-title` (Third party; type `String`): The Document title.
- `springdoc.swagger-ui.enabled` (Third party; type `Boolean`; default `false`): Whether to generate and serve an OpenAPI document.
- `springdoc.swagger-ui.filter` (Third party; type `String`): If set, enables filtering. The top bar will show an edit box that could be used to filter the tagged operations that are shown.
- `springdoc.swagger-ui.groups-order` (Third party; type `AbstractSwaggerUiConfigProperties.Direction`; default `asc`): The Groups order.
- `springdoc.swagger-ui.layout` (Third party; type `String`): The name of a component available via the plugin system to use as the top-level layout for Swagger UI.
- `springdoc.swagger-ui.max-displayed-tags` (Third party; type `Integer`): If set, limits the number of tagged operations displayed to at most this many.
- `springdoc.swagger-ui.oauth.additional-query-string-params` (Third party; type `Map<String,String>`): The Additional query string params.
- `springdoc.swagger-ui.oauth.app-name` (Third party; type `String`): The App name.
- `springdoc.swagger-ui.oauth.client-id` (Third party; type `String`): The Client id.
- `springdoc.swagger-ui.oauth.client-secret` (Third party; type `String`): The Client secret.
- `springdoc.swagger-ui.oauth.config-parameters` (Third party; type `Map<String,Object>`)
- `springdoc.swagger-ui.oauth.realm` (Third party; type `String`): The Realm.
- `springdoc.swagger-ui.oauth.scope-separator` (Third party; type `String`): The Scope separator.
- `springdoc.swagger-ui.oauth.scopes` (Third party; type `List<String>`): The Scopes selected by default upon authentication.
- `springdoc.swagger-ui.oauth.use-basic-authentication-with-access-code-grant` (Third party; type `Boolean`): The Use basic authentication with access code grant.
- `springdoc.swagger-ui.oauth.use-pkce-with-authorization-code-grant` (Third party; type `Boolean`): The Use pkce with authorization code grant.
- `springdoc.swagger-ui.oauth2-redirect-url` (Third party; type `String`): OAuth redirect URL.
- `springdoc.swagger-ui.operations-sorter` (Third party; type `String`): Apply a sort to the operation list of each API
- `springdoc.swagger-ui.path` (Third party; type `String`): The path for the Swagger UI pages to load.
- `springdoc.swagger-ui.persist-authorization` (Third party; type `Boolean`): The Persist authorization.
- `springdoc.swagger-ui.query-config-enabled` (Third party; type `Boolean`): The Query config enabled.
- `springdoc.swagger-ui.show-common-extensions` (Third party; type `Boolean`): Controls the display of extensions
- `springdoc.swagger-ui.show-extensions` (Third party; type `Boolean`): Controls the display of vendor extension (x-) fields and values.
- `springdoc.swagger-ui.supported-submit-methods` (Third party; type `List<String>`): The supported try it out methods
- `springdoc.swagger-ui.syntax-highlight.activated` (Third party; type `Boolean`): The Activated.
- `springdoc.swagger-ui.syntax-highlight.theme` (Third party; type `String`): The Theme.
- `springdoc.swagger-ui.tags-sorter` (Third party; type `String`): Apply a sort to the tag list of each API
- `springdoc.swagger-ui.try-it-out-enabled` (Third party; type `Boolean`): Try it out enabled
- `springdoc.swagger-ui.url` (Third party; type `String`): The Url.
- `springdoc.swagger-ui.urls` (Third party; type `Set<AbstractSwaggerUiConfigProperties.SwaggerUrl>`): The Urls.
- `springdoc.swagger-ui.urls-primary-name` (Third party; type `String`): The Urls primary name.
- `springdoc.swagger-ui.use-root-path` (Third party; type `Boolean`; default `false`): The Use root path.
- `springdoc.swagger-ui.validator-url` (Third party; type `String`): URL to validate specs against.
- `springdoc.swagger-ui.version` (Third party; type `String`): The Swagger ui version.
- `springdoc.swagger-ui.with-credentials` (Third party; type `Boolean`): The With credentials.
- `springdoc.trim-kotlin-indent` (Third party; type `Boolean`; default `false`): The trim kotlin indent.
- `springdoc.use-arbitrary-schemas` (Third party; type `Boolean`; default `false`): When set to true, schemas without a defined type will be deserialized as an ArbitrarySchema (with no type), instead of an ObjectSchema with type: object.
- `springdoc.use-fqn` (Third party; type `Boolean`; default `false`): Use fully qualified name
- `springdoc.use-management-port` (Third party; type `Boolean`; default `false`): If set to true, exposes the swagger-ui on the actuator management port.
- `springdoc.writer-with-default-pretty-printer` (Third party; type `Boolean`; default `false`): The Writer with default pretty printer.
- `springdoc.writer-with-order-by-keys` (Third party; type `Boolean`; default `false`): The Writer with order by keys.

**Groovy scripting**

CAS takes advantage of Apache Groovy in forms of either embedded or external scripts that allow one to, by default, dynamically build constructs, attributes, access strategies and a lot more. To activate the functionality described here, you may need to prepare CAS to support and integrate with Apache Groovy.

Please [review this guide](https://apereo.github.io/cas/development/integration/Apache-Groovy-Scripting.html) to configure your build.

## Endpoints

The following Swagger endpoints may be used to analyze and test the APIs:

| Description | URL |
| --- | --- |
| Swagger API Specification | `https://sso.example.org/cas/v3/api-docs` |
| Swagger UI | `https://sso.example.org/cas/swagger-ui.html` |
