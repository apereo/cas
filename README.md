# Central Authentication Service (CAS)

<http://www.jasig.org/cas>

## Introduction

Welcome to the home of the Central Authentication Service project, more commonly referred to as CAS.  The Central Authentication Service (CAS) is the standard mechanism by which web applications should authenticate users. 

CAS provides enterprise single sign-on service:

- An open and well-documented protocol
- An open-source Java server component
- A library of clients for Java, .Net, PHP, Perl, Apache, uPortal, and others
- Integrates with uPortal, BlueSocket, TikiWiki, Mule, Liferay, Moodle and others
- Community documentation and implementation support
- An extensive community of adopters

## Build [![Build Status](https://api.travis-ci.org/Jasig/cas.png)](http://travis-ci.org/Jasig/cas) [![Codeship Status for Jasig/cas](https://www.codeship.io/projects/a204a3a0-727c-0131-ab14-4e46b2fa20d2/status)](https://www.codeship.io/projects/13661)
It is recommended to build and deploy CAS locally using the [Maven War Overlay method][overlay]. 
This approach does not require the adopter to *explicitly* download any version of CAS, but 
rather utilizes Maven's overlay mechanism to combine CAS original artifacts and local 
customizations to further ease future upgrades and maintenance.

## Download
- Binary releases may be downloaded from [here](http://www.jasig.org/cas/download).
- CAS artifacts are published through the [Maven Central Repository.](http://mvnrepository.com/artifact/org.jasig.cas)
- A snapshot of the codebase's `master` branch may be downloaded from [here](https://github.com/Jasig/cas/archive/master.zip).
- Snapshot artifacts are also published through the [Sonatype snapashots repository](https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/) under the group id **`org.jasig.cas`**.
- The codebase may also be *cloned* using a Git client via the following command:
```bash
git clone git@github.com:Jasig/cas.git
```

**Note:** If building CAS from the source, running the test cases currently requires an active Internet connection.
Please [see the maven docs][skip] on how to disable the tests.


## Documentation
- [Official Documentation][wiki]
- [Release Notes](https://issues.jasig.org/secure/ReleaseNote.jspa?projectId=10007)

## Addons
- [CAS Addons](https://github.com/Unicon/cas-addons) is an open source collection of useful CAS server extensions.

## Contributions
- [How to contribute to Jasig](http://www.jasig.org/jasig/contribute)

[wiki]: http://jasig.github.io/cas
[overlay]: http://jasig.github.io/cas/current/installation/Maven-Overlay-Installation.html
[skip]: http://maven.apache.org/general.html#skip-test
