# User Interface - Thymeleaf

Source: https://apereo.github.io/cas/development/ux/User-Interface-Thymeleaf.html

CAS uses [Thymeleaf](https://www.thymeleaf.org/) for its markup rendering engine. Each template is decorated by `layout.html` template file, which provides a layout structure for the template’s content. Individual components optimized for reuse among multiple templates are stored in the `src/main/resources/templates/fragments` folder, and referenced by the templates in `src/main/resources/templates`.

Refer to the [Thymeleaf documentation](https://www.thymeleaf.org/) for more information on its use and syntax.

The following settings and properties are available from the CAS configuration catalog:

- `spring.thymeleaf.cache` (Third party; type `Boolean`; default `true`): Whether to enable template caching.
- `spring.thymeleaf.check-template` (Third party; type `Boolean`; default `true`): Whether to check that the template exists before rendering it.
- `spring.thymeleaf.check-template-location` (Third party; type `Boolean`; default `true`): Whether to check that the templates location exists.
- `spring.thymeleaf.enable-spring-el-compiler` (Third party; type `Boolean`; default `false`): Enable the SpringEL compiler in SpringEL expressions.
- `spring.thymeleaf.enabled` (Third party; deprecation `ERROR`, no replacement)
- `spring.thymeleaf.encoding` (Third party; type `Charset`; default `UTF-8`): Template files encoding.
- `spring.thymeleaf.excluded-view-names` (Third party; type `String[]`): List of view names (patterns allowed) that should be excluded from resolution.
- `spring.thymeleaf.mode` (Third party; type `String`; default `HTML`): Template mode to be applied to templates. See also Thymeleaf's TemplateMode enum.
- `spring.thymeleaf.prefix` (Third party; type `String`; default `classpath:/templates/`): Prefix that gets prepended to view names when building a URL.
- `spring.thymeleaf.reactive.chunked-mode-view-names` (Third party; type `String[]`): Comma-separated list of view names (patterns allowed) that should be the only ones executed in CHUNKED mode when a max chunk size is set.
- `spring.thymeleaf.reactive.full-mode-view-names` (Third party; type `String[]`): Comma-separated list of view names (patterns allowed) that should be executed in FULL mode even if a max chunk size is set.
- `spring.thymeleaf.reactive.max-chunk-size` (Third party; type `DataSize`; default `0B`): Maximum size of data buffers used for writing to the response. Templates will execute in CHUNKED mode by default if this is set.
- `spring.thymeleaf.reactive.media-types` (Third party; type `List<MediaType>`; default `["text/html", "application/xhtml+xml", "application/xml", "text/xml", "application/rss+xml", "application/atom+xml", "application/javascript", "application/ecmascript", "text/javascript", "text/ecmascript", "application/json", "text/css", "text/plain", "text/event-stream"]`): Media types supported by the view technology.
- `spring.thymeleaf.render-hidden-markers-before-checkboxes` (Third party; type `Boolean`; default `false`): Whether hidden form inputs acting as markers for checkboxes should be rendered before the checkbox element itself.
- `spring.thymeleaf.servlet.content-type` (Third party; type `MimeType`; default `text/html`): Content-Type value written to HTTP responses.
- `spring.thymeleaf.servlet.produce-partial-output-while-processing` (Third party; type `Boolean`; default `true`): Whether Thymeleaf should start writing partial output as soon as possible or buffer until template processing is finished.
- `spring.thymeleaf.suffix` (Third party; type `String`; default `.html`): Suffix that gets appended to view names when building a URL.
- `spring.thymeleaf.template-resolver-order` (Third party; type `Integer`): Order of the template resolver in the chain. By default, the template resolver is first in the chain. Order start at 1 and should only be set if you have defined additional "TemplateResolver" beans.
- `spring.thymeleaf.view-names` (Third party; type `String[]`): List of view names (patterns allowed) that can be resolved.
