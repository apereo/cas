---
layout: default
title: CAS - Test Process
category: Developer
---

{% include variables.html %}

# Test Process

This page documents the steps that a CAS developer/contributor should take for testing a CAS 
server deployment during development. For additional
instructions and guidance on the general build process, please [see this page](Build-Process.html).

<div class="alert alert-info"><strong>Contributions</strong><p>Patches submitted to the CAS codebase 
in form of pull requests must pass all automated unit or integration tests, and/or 
provide adequate unit or integration tests for the proposed changes. In the absence of appropriate test cases,
the contribution most likely will not be accepted into the codebase and ultimately may be closed.</p></div>

## Testing Modules

To test the functionality provided by a given CAS module, execute the following steps:

- For the tomcat, undertow or jetty webapp, add the module reference to the `webapp.gradle` build script of web application you intend to run:

```gradle
implementation project(":support:cas-server-support-modulename")
```

Alternatively, pass the required modules automatically: 

```bash
bc ldap,x509
```

...where `bc` is an [alias for building CAS](Build-Process.html#sample-build-aliases).

Prepare [the embedded container](Build-Process.html#embedded-containers), to run and deploy the web application.

## Unit / Integration Testing

To simplify the test execution process, you may take advantage of the `testcas.sh` script found at the root of the repository as such:

```bash
# chmod +x ./testcas.sh
./testcas.sh --category <category> [--test <test-class>] [--debug] [--with-coverage]
```

To learn more about the script, use:

```bash
./testcas.sh --help
```

All unit and integration tests are executed by the [continuous integration system](Test-Process.html#continuous-integration).

## Code Coverage & Metrics

Code coverage metrics are collected and reported by the following platforms:

| System                            | Badge
|-----------------------------------+---------------------------------------------------------------------------+
| Codacy           | [![Codacy Badge](https://app.codacy.com/project/badge/Coverage/29973e19266547dab7ab73f1a511c826)](https://www.codacy.com/gh/apereo/cas/dashboard?utm_source=github.com&utm_medium=referral&utm_content=apereo/cas&utm_campaign=Badge_Coverage)
| SonarCloud           | [![Coverage](https://sonarcloud.io/api/project_badges/measure?project=org.apereo.cas%3Acas-server&metric=coverage)](https://sonarcloud.io/dashboard?id=org.apereo.cas%3Acas-server)
| CodeCov           | [![codecov](https://codecov.io/gh/apereo/cas/branch/master/graph/badge.svg)](https://codecov.io/gh/apereo/cas)

Quality metrics are collected and reported by the following platforms:

| System                            | Badge
|-----------------------------------+---------------------------------------------------------------------------+
| Codacy           | [![Codacy Badge](https://app.codacy.com/project/badge/Grade/29973e19266547dab7ab73f1a511c826)](https://www.codacy.com/gh/apereo/cas/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=apereo/cas&amp;utm_campaign=Badge_Grade)
| SonarCloud Quality Gate           | [![Sonarqube Quality](https://sonarcloud.io/api/project_badges/measure?project=org.apereo.cas%3Acas-server&metric=alert_status)](https://sonarcloud.io/dashboard?id=org.apereo.cas%3Acas-server)
| SonarCloud Maintainability            | [![Sonarqube Quality](https://sonarcloud.io/api/project_badges/measure?project=org.apereo.cas%3Acas-server&metric=sqale_rating)](https://sonarcloud.io/dashboard?id=org.apereo.cas%3Acas-server) 

## Browser & Functional Testing

Automated browser testing is done via the [Puppeteer framework](https://pptr.dev/). Puppeteer is a Node library which provides a high-level 
API to control Chrome or Chromium over the DevTools Protocol and runs headless by default.

Functional tests start by generating a plain CAS overlay as a baseline that is able to run under HTTPS using a pre-generated keystore.
This overlay is supplied the test scenario configuration that explains the required modules, properties, etc to use when CAS is deployed
inside an embedded Apache Tomcat container. Once running, the Puppeteer script is executed by Node for the given test scenario to verify
specific functionality such as successful logins, generation of tickets, etc.

All functional and browser tests are executed by the [continuous integration system](Test-Process.html#continuous-integration). 
  
To install Puppeteer once:

```bash
npm i -g puppeteer
```

To help simplify the testing process, you may use the following bash function in your `~/.profile`:

```bash
function pupcas() {
  scenario=$1
  cd /path/to/cas 
  ./ci/tests/puppeteer/run.sh ./ci/tests/puppeteer/scenarios/"${scenario}"
}
```

...which can later be invoked as:

```bash
pupcas <scenario-name>
```
                                 
To see the list of available test scenarios:

```bash
./gradlew --build-cache --configure-on-demand --no-daemon -q puppeteerScenarios
```

Remote debugging is available on port `5000`. To successfully run tests, 
you need to make sure [jq](https://stedolan.github.io/jq/) is installed.

### MacOS Firewall Popup
                      
To allow the firewall to accept incoming network connections for Chromium on MacOS, 
you may apply the following command:

```bash
chromium="/path/to/cas/ci/tests/puppeteer/node_modules/puppeteer/.local-chromium"
sudo codesign --force --deep --sign - "${chromium}/mac-*/chrome-mac/Chromium.app"
```

## Continuous Integration

Unit and integration tests are automatically executed by the CAS CI system, [GitHub Actions](https://github.com/apereo/cas/actions).
