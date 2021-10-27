# Central Authentication Service (CAS)

<http://www.jasig.org/cas>

## Introduction

The Central Authentication Service (CAS) is the standard mechanism by which web
applications should authenticate users. Any custom applications written benefit
from using CAS.

Note that CAS provides authentication; that is, it determines that your users
are who they say they are. CAS should not be viewed as an access-control system;
in particular, providers of applications that grant access to anyone who
possesses a NetID should understand that loose affiliates of an organization may
be granted NetIDs.

## Release info

CAS requires J2SE 1.5 and J2EE1.3.

## Distribution JAR files

The "modules" directory contains distinct jars for each Maven module.  It also
contains a war file that can be used for demo purposes.

## Dependencies

## Deployment

* Servlet Container that can handle JSP 2.0 (e.g., Tomcat 5.0.28)

## Note

If building CAS from the source, running the test cases currently
requires an active Internet connection.

Please [see the maven docs][skip] on how to disable the tests.

[skip]: http://maven.apache.org/general.html#skip-test

