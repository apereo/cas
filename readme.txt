CENTRAL AUTHENTICATION SERVICE (CAS)
--------------------------------------------------------------------
http://www.yale.edu/tp/auth/

1.  INTRODUCTION

The Central Authentication Service (CAS) is the standard mechanism by which web
applications should authenticate users. Any custom applications written benefit
from using CAS.

Note that CAS provides authentication; that is, it determines that your users
are who they say they are. CAS should not be viewed as an access-control system;
in particular, providers of applications that grant access to anyone who
possesses a NetID should understand that loose affiliates of an organization may
be granted NetIDs.

2.  RELEASE INFO

CAS requires J2SE 1.4 and J2EE1.3.

Release conents:
* "src" contains the Java source files for the framework
* "test" contains the Java source files for CAS's test suite TODO
* "dist" contains various CAS distribution jar files
* "docs" contains general documentation and API javadocs

3. DISTRIBUTION JAR FILES


The "dist" directory contains the following distinct jar files and wars:

* cas-spring-server.jar 
- Contents: CAS controllers managers, domain objects, etc.

* cas.war
- Contents: complete web application ready for deployment.

4.   DEPENDENCIES
* Commons HttpClient 2.0
* EhCache 1.0
* J2EE Servlet 2.3+
* Spring 1.1.2
* Servlet Container that can handle JSP 2.0 (i.e. Tomcat 5.0.28)



