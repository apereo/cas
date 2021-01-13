```properties
# spring.thymeleaf.encoding=UTF-8

# Controls  whether views should be cached by CAS.
# When turned on, ad-hoc chances to views are not automatically
# picked up by CAS until a restart. Small incremental performance
# improvements are to be expected.
# spring.thymeleaf.cache=true

# Instruct CAS to locate views at the below location.
# This location can be externalized to a directory outside
# the cas web application.
# spring.thymeleaf.prefix=classpath:/templates/

# Defines a default URL to which CAS may redirect 
# if there is no service provided in the authentication request.
# cas.view.default-redirect-url=https://www.github.com

# CAS views may be located at the following paths outside
# the web application context, in addition to prefix specified
# above which is handled via Thymeleaf.
# cas.view.template-prefixes[0]=file:///etc/cas/templates
```

The following settings control the view configuration for CAS `v1`:

{% include casproperties.html properties="cas.view.cas1" %}

```properties
# cas.view.cas1.attribute-renderer-type=DEFAULT|VALUES_PER_LINE
```

The following settings control the view configuration for CAS `v2`:

{% include casproperties.html properties="cas.view.cas2" %}

The following settings control the view configuration for CAS `v3`:

{% include casproperties.html properties="cas.view.cas3" %}
