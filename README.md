# Central Authentication Service (CAS) [![License](https://img.shields.io/hexpm/l/plug.svg)](https://github.com/Jasig/cas/blob/master/LICENSE)
[![](https://heroku-badge.herokuapp.com/?app=jasigcas)](http://jasigcas.heroku.com)
[![](https://heroku-badge.herokuapp.com/?app=jasigcasmgmt)](http://jasigcasmgmt.heroku.com)

## Introduction

Welcome to the home of the [Central Authentication Service project](http://www.apereo.org/cas), more commonly referred to as CAS. The Central Authentication Service (CAS) is the standard mechanism by which web applications should authenticate users. 

CAS provides enterprise single sign-on service:

- An open and well-documented protocol
- An open-source Java server component
- A library of clients for Java, .Net, PHP, Perl, Apache, uPortal, and others
- Integrates with uPortal, BlueSocket, TikiWiki, Mule, Liferay, Moodle and others
- Community documentation and implementation support
- An extensive community of adopters

## Build [![Build Status](https://api.travis-ci.org/Jasig/cas.png)](http://travis-ci.org/Jasig/cas) [![Codeship Status for Jasig/cas](https://www.codeship.io/projects/a204a3a0-727c-0131-ab14-4e46b2fa20d2/status)](https://www.codeship.io/projects/13661) [![Dependency Management](https://www.versioneye.com/user/projects/54ac6d74b6c7ff65b8000072/badge.svg?style=flat)](https://www.versioneye.com/user/projects/54ac6d74b6c7ff65b8000072) [![Issue Stats](http://www.issuestats.com/github/Jasig/cas/badge/pr?style=flat)](http://www.issuestats.com/github/Jasig/cas) [![Issue Stats](http://www.issuestats.com/github/Jasig/cas/badge/issue?style=flat)](http://www.issuestats.com/github/Jasig/cas)

It is recommended to build and deploy CAS locally using the [Maven War Overlay method][overlay]. 
This approach does not require the adopter to *explicitly* download any version of CAS, but 
rather utilizes Maven's overlay mechanism to combine CAS original artifacts and local 
customizations to further ease future upgrades and maintenance.

## Download [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server/badge.svg?style=flat)](https://maven-badges.herokuapp.com/maven-central/org.jasig.cas/cas-server) 

- Binary releases may be downloaded from [here][downloadcas].
- CAS artifacts are published to the [Maven Central Repository][casmavencentral].
- Remember that the [Maven War Overlay method][overlay] is the recommended approach for deployments.
- A snapshot of the codebase's `master` branch may be downloaded from [here][downloadcasgithub].
- Snapshot artifacts are also published through the [Sonatype snapashots repository][cassonatype] under the group id **`org.jasig.cas`**.
- The codebase may also be *cloned* using a Git client via the following command:
```bash
git clone git@github.com:Jasig/cas.git
```

**Note:** If building CAS from the source, running the test cases currently requires an active Internet connection.
Please [see the maven docs][skip] on how to disable the tests.

## Documentation 
- [Official Documentation][wiki]
- [Release Notes][releasenotes]
- [![Gitter](https://badges.gitter.im/Join Chat.svg)](https://gitter.im/Jasig/cas?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

## Addons
- [CAS Addons][casaddons] is an open source collection of useful CAS server extensions.

## Contributions
- [How to contribute][contribute]

[wiki]: http://jasig.github.io/cas
[overlay]: http://jasig.github.io/cas/development/installation/Maven-Overlay-Installation.html
[skip]: http://maven.apache.org/general.html#skip-test
[contribute]: http://jasig.github.io/cas/developer/Contributor-Guidelines.html
[downloadcas]: http://www.apereo.org/cas/download
[cassonatype]: https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/
[casmavencentral]: http://mvnrepository.com/artifact/org.jasig.cas
[downloadcasgithub]: https://github.com/Jasig/cas/archive/master.zip
[releasenotes]: https://github.com/Jasig/cas/releases
[casaddons]: https://github.com/unicon-cas-addons
