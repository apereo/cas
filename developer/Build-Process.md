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
./gradlew build --parallel -x test -x javadoc -DskipCheckstyle=true -DskipAspectJ=true -DskipFindbugs=true
{% endhighlight %}

The following commandline flags are supported by the build:

| Flag                              | Description
|-----------------------------------+----------------------------------------------------+
| `skipCheckstyle`                  | Skip running checkstyle checks. 
| `skipTests`                       | Skip running JUnit tests, but compile them. 
| `skipAspectJ`                     | Skip decorating source files with AspectJ.
| `skipFindbugs`                    | Skip running findbugs checks. 
| `skipVersionConflict`             | If a dependency conflict is found, use the latest version rather than failing the build. 

Note that you can use `-x <task>` to entirely skip/ignore a phase in the build. (i.e. `-x test/javadoc`)
