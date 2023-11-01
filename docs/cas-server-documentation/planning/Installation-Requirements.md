---
layout: default
title: CAS - Installation Requirements
category: Installation
---

{% include variables.html %}

# Installation Requirements

Depending on choice of configuration components, there may be additional requirements such as LDAP directory,
database, and caching infrastructure. In most cases, however, requirements should be self evident to deployers who
choose components with clear hardware and software dependencies. In any case where additional requirements are
not obvious, the discussion of component configuration should mention system, software, hardware, and other
requirements.

## Java

CAS at its heart is a Java-based web application. Prior to deployment, you will need to have [JDK](https://openjdk.java.net/projects/jdk/21/) `21` installed. The JDK requirement
is global and applies to all CAS modules and features. The required Java version must be available both at compile-time when you build and package CAS and of course at runtime when
you deploy the CAS web application.

<div class="alert alert-info">:information_source: <strong>Release Policy</strong><p>
Java platform requirements typically only change and require newer versions when CAS major releases are published.
The requirements will universally remain the same for all other feature, minor, patch or maintenance releases.
Furthermore, we cannot in good confidence ascertain that CAS would run correctly in newer JDK versions above the required baseline. YMMV.
</p></div>

All compatible distributions such as Amazon Corretto, Zulu, Eclipse Temurin, etc should work and are implicitly supported. Whatever Java distribution you use,
please make sure you have reviewed the license agreement carefully before downloading and using the package.

## Servlet Containers

There is no officially supported servlet container for CAS, but [Apache Tomcat](http://tomcat.apache.org/) is the most
commonly used. Support for a particular servlet container depends on the expertise of community members.

See [this guide](../installation/Configuring-Servlet-Container.html) for more info.

## Build Tools

WAR overlays are [provided](../installation/WAR-Overlay-Installation.html) to allow for a straightforward and flexible 
deployment solution. While it admittedly requires a high up-front cost in learning, it reaps numerous benefits in the long run. 

<div class="alert alert-info">:information_source: <strong>Do Less</strong><p>
You <b>DO NOT</b> need to have Gradle installed prior to the installation. It is provided to you automatically.
</p></div>

## Git (Optional)

While not strictly a requirement, it's HIGHLY recommended that you have [Git](https://git-scm.com/downloads) installed for your CAS deployment,
and manage all CAS artifacts, configuration files, build scripts and setting inside a source control repository.

## OS

No particular preference on the operating system, though Linux-based installs are typically more common than Windows.

## Internet Connectivity

Internet connectivity is generally required for the build phase of any Maven/Gradle based project, including the 
recommended WAR overlays used to install CAS. The build process resolves dependencies by searching online repositories 
containing artifacts (jar files in most cases) that are downloaded and installed locally.

## Hardware

Anecdotal community evidence seems to suggest that CAS deployments would perform well on a dual-core 3.00Ghz 
processor with 4GB of memory, at a minimum. Enough disk space (preferably SSD) is also 
needed to house CAS-generated logs, if logs are kept on the server itself.

Remember that the above requirements are *suggestions*. You may get by perfectly fine with more or less, 
depending on your deployment and request volume. Start with the bare minimum and be prepared to adjust and strengthen capacity on demand if needed.
