---
layout: default
title: CAS - Metrics
category: Monitoring & Statistics
---

{% include variables.html %}

# Custom - CAS Metrics

To register custom metrics, inject `MeterRegistry` into your component, as shown in the following example:

```java
public class Dictionary {
    private final List<String> words = new CopyOnWriteArrayList<>();

    Dictionary(final MeterRegistry registry) {
        registry.gaugeCollectionSize("dictionary.size", Tags.empty(), this.words);
    }
}
```

If you find that you repeatedly instrument a suite of metrics across components or applications,
you may encapsulate this suite in a `MeterBinder` implementation. By default, metrics
from all `MeterBinder` beans will be automatically bound to the Spring-managed `MeterRegistry`.

If you need to apply customizations to specific Meter instances 
you can use the `io.micrometer.core.instrument.config.MeterFilter` interface. 
By default, all `MeterFilter` beans will be automatically applied to the micrometer `MeterRegistry.Config`.

For example, if you want to rename the `mytag.region` tag to `mytag.area` for all 
meter IDs beginning with `com.example`, you can do the following:

```java
@Bean
public MeterFilter renameRegionTagMeterFilter() {
    return MeterFilter.renameTag("com.example", "mytag.region", "mytag.area");
}
```
