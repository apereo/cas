---
layout: default
title: CAS - 8.1.0-RC4 Release Notes
category: Planning
release:
  line: 8.1.0 development
  summary: >-
    The fourth release candidate is in progress. This page fills in as changes land on the development branch.
  facts:
    - label: Requires
      value: JDK 25
      url: ../planning/Installation-Requirements.html
    - value: "556"
      suffix: browser test scenarios
      url: ../../developer/Test-Process.html
---

{% include variables.html %}

{% include release-digest.html %}

## New & Noteworthy

The following items are new improvements and enhancements presented in this release.

### OpenRewrite Recipes
{: data-area="operations"}

CAS continues to produce and publish [OpenRewrite](https://docs.openrewrite.org/) recipes that allow the project to upgrade installations
in place from one version to the next. [See this guide](../installation/OpenRewrite-Upgrade-Recipes.html) to learn more.

### Graal VM Native Images
{: data-area="operations"}

A CAS server installation and deployment process can be tuned to build and run
as a [Graal VM native image](../installation/GraalVM-NativeImage-Installation.html). We continue to polish native runtime hints.
The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) have selectively switched
to build and verify Graal VM native images and we plan to extend the coverage to all such scenarios in the coming releases.

### Testing Strategy
{: data-area="project"}

The collection of end-to-end [browser tests based on Puppeteer](../../developer/Test-Process.html) continue to grow to cover more use cases
and scenarios. At the moment, total number of jobs stands at approximately `556` distinct scenarios. The overall
test coverage of the CAS codebase is approximately `94%`.


## Other Stuff

{% include release-footer.html %}
