---
layout: default
title: CAS - Overlay Installation
category: Installation
---
{% include variables.html %}


# Installation

CAS installation is a fundamentally source-oriented process, and we recommend a WAR overlay (1) 
project to organize customizations such as component configuration and UI design.
The output of a WAR overlay build is a `cas.war` file that can be deployed 
to a servlet container like [Apache Tomcat](Configuring-Servlet-Container.html).

## Requirements

[See this guide](../planning/Installation-Requirements.html) to learn more.

## What is a WAR Overlay?

Overlays are a strategy to combat repetitive code and/or resources. Rather than downloading 
the CAS codebase and building from source,
overlays allow you to download a pre-built vanilla CAS web application server provided by the project itself and override/insert specific behavior into it. At build time, the build 
installation process will attempt to download the provided 
binary artifact first. Then the tool will locate your configuration files and settings made available 
inside the same project directory and will merge those into the downloaded artifact in order to produce
one wholesome archive (i.e. `cas.war`) . Overridden artifacts may include 
resources, java classes, images, CSS and javascript files. In order for the merge
process to successfully execute, the location and names of the overridden artifacts 
locally must **EXACTLY** match that of those provided by the project
inside the originally downloaded archive. Java code in the overlay project's `src/main/java` 
folder and resources in `src/main/resources` will end up in the `WEB-INF\classes` 
folder of cas.war and they will be loaded by the classloader instead of 
resources with the same names in jar files inside `WEB-INF\lib`.  

It goes without saying that while up-front ramp-up time could be slightly complicated, there are significant advantages to this approach:

1. There is no need to download/build from the source.
2. Upgrades are tremendously easier in most cases by adjusting the build script to download the newer CAS release.
3. Rather than hosting the entire software source code, as the deployer you **ONLY** keep your own local customizations which makes change tracking much easier.
4. Tracking changes inside a source control repository is very lightweight, again because only relevant changes (and not the entire software) is managed.

### Managing Overlays

Most if not all aspects of CAS can be controlled by adding, removing, or 
modifying files in the overlay; it's also possible and indeed common to customize the behavior of
CAS by adding third-party components that implement CAS APIs as Java source files or dependency references.

The process of working with an overlay can be summarized in the following steps:

- Start with and build the provided basic vanilla build/deployment.
- Identify the artifacts from the produced build that need changes. These artifacts are generally produced by the build in the `build` directory for Gradle.
- Copy the identified artifacts from the identified above directories over to the `src/main/resources` directory.
1. Create the `src/main/resources` directories, if they don't already exist.
2. Copied paths and file names **MUST EXACTLY MATCH** their build counterparts, or the change won't take effect.
- After changes, rebuild and repeat the process as many times as possible.
- Double check your changes inside the built binary artifact to make sure the overlay process is working.

<div class="alert alert-warning">:warning: <strong>Be Exact</strong><p>Do NOT copy everything produced by 
the build. Attempt to keep changes and customizations to a 
minimum and only grab what you actually need. Make sure the deployment environment 
is kept clean and precise, or you incur the risk of terrible upgrade issues and painful headaches.</p></div>

## CAS Overlay Initializr

Apereo CAS Initializr is a service provided by the Apereo CAS project that allows you 
as the deployer to generate CAS WAR Overlay projects on the fly with just what you need to start quickly.

To learn more about the initializr, please [review this guide](WAR-Overlay-Initializr.html).

## Dockerized Deployment

See [this guide](Docker-Installation.html) for more info.

## Servlet Container

CAS can be deployed to a number of servlet containers. See [this guide](Configuring-Servlet-Container.html) for more info.

## Dependency Management

Each release of CAS provides a curated list of dependencies it supports. In practice, you do not need to provide a version for any of
these dependencies in your build configuration as the CAS distribution is managing that for you. When you 
upgrade CAS itself, these dependencies will be upgraded as well in a consistent way.

The curated list of dependencies contains a refined list of third party libraries. The list is 
available as a standard Bills of Materials (BOM). Not everyone likes inheriting from the BOM.
You may have your own corporate standard parent that you need to use, or you may just prefer to explicitly declare all your configuration.

To take advantage of the CAS BOM, please see [this guide](BOM-Dependency-Management.html).

<sub>(1) [WAR Overlays](http://maven.apache.org/plugins/maven-war-plugin/overlays.html)</sub>
