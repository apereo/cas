---
layout: default
title: CAS - Build Process
---

# Build Process
This page documents the steps that a CAS developer should take for building a CAS server locally.

## Source Checkout
The following shell commands may be used to grab the source from the repository:

{% highlight bash %}
git clone git@github.com:Jasig/cas.git cas-server
{% endhighlight %}

## Executing Build
The following shell commands may be used to build the source:

{% highlight bash %}
cd cas-server
./gradlew build --parallel -x test -DskipCheckstyle=true -DskipJavadocs=true -DskipAspectJ=true -DskipFindbugs=true
{% endhighlight %}

| Flag                              | Description
|-----------------------------------+----------------------------------------------------+
| `skipCheckstyle`                  | Skip running checkstyle checks. 
| `skipJavadocs  `                  | Skip generating javadocs.
| `skipAspectJ`                     | Skip decorating source files with AspectJ 
| `skipFindbugs`                    | Skip running findbugs checks. 
| `skipVersionConflict`             | If a dependency conflict is found, use the latest version rather than failing the build. 
