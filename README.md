# Central Authentication Service (CAS)

<http://www.jasig.org/cas>

## Introduction

Welcome to the home of  the Central Authentication Service project, more commonly referred to as CAS.  The Central Authentication Service (CAS) is the standard mechanism by which web applications should authenticate users. 

CAS provides enterprise single sign-on service:

- [An open and well-documented protocol][protocol]
- An open-source Java server component
- [A library of clients for Java, .Net, PHP, Perl, Apache, uPortal, and others](https://wiki.jasig.org/display/CASC/Official+Clients)
- [Integrates with uPortal, BlueSocket, TikiWiki, Mule, Liferay, Moodle and others](https://wiki.jasig.org/display/CAS/CASifying+Applications)
- Community documentation and implementation support
- An extensive community of adopters

## Requirements
CAS requires J2SE 1.6, J2EE1.3 and a servlet container that can handle JSP 2.0 (e.g., Tomcat 5.0.28)

## Build [![Build Status](https://api.travis-ci.org/Jasig/cas.png)](http://travis-ci.org/Jasig/cas)
It is recommended to build and deploy CAS locally using the [Maven War Overlay method](https://wiki.jasig.org/display/CASUM/Best+Practice+-+Setting+Up+CAS+Locally+using+the+Maven2+WAR+Overlay+Method). This approach does not require the adopter to *explicitly* download any version of CAS, but rather utilizes Maven's overlay mechanism to combine CAS original artifacts and local customozations to further ease future upgrades and maintenance.

## Download
- Binary releases may be downloaded from [here](http://www.jasig.org/cas/download).
- CAS artifacts are published through the [Maven Central Repository.](http://mvnrepository.com/artifact/org.jasig.cas)
- A snapshot of the codebase's `master` branch may be downloaded from [here](https://github.com/Jasig/cas/archive/master.zip).
- Snapshot artifacts are also published through the [Sonatype snapashots repository](https://oss.sonatype.org/content/repositories/snapshots/org/jasig/cas/) under the group id **`org.jasig.cas`**.
- The codebase may also be *cloned* using a Git client via the following command:
`git clone git@github.com:Jasig/cas.git`

**Note:** If building CAS from the source, running the test cases currently requires an active Internet connection.
Please [see the maven docs][skip] on how to disable the tests.


## Documentation
- [CAS Protocol][protocol]
- [CAS User Manual](https://wiki.jasig.org/display/CASUM/Home)
- [Javadocs](https://oss.sonatype.org/content/repositories/releases/org/jasig/cas/) 
Javadocs may also be created locally using the Maven command: `mvn clean site site:stage` and will then be available at the `target/staging` folder of the root project directory.
- [Release Notes](https://issues.jasig.org/secure/ReleaseNote.jspa?projectId=10007)

## Addons
- [CAS Addons](https://github.com/Unicon/cas-addons) is an open source collection of useful JASIG-CAS server addons.

## Contributions
- [How to contribute to Jasig](http://www.jasig.org/jasig/contribute)
- [Contributor guidelines](https://github.com/Jasig/cas/wiki/Contributor-Guidelines)
- [CAS code conventions](https://wiki.jasig.org/display/CAS/Code+Conventions)

[protocol]: http://www.jasig.org/cas/protocol
[skip]: http://maven.apache.org/general.html#skip-test
