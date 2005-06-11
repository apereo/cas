CENTRAL AUTHENTICATION SERVICE (CAS)
--------------------------------------------------------------------
http://jasigch.princeton.edu:9000/display/CAS

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
* "core/src/main" contains the Java source files for the framework
* "core/src/test" contains the Java source files for CAS's test suite
* "docs" contains general documentation and API javadocs
* "target" contains the demo war file
* "localPlugins" contains the location for user modifications

3. DISTRIBUTION JAR FILES

The "target" directory contains the following distinct jar files and wars:
* cas.war
- Contents: complete web application ready for deployment.

4.   DEPENDENCIES

* EhCache 1.1
* J2EE Servlet 2.3+
* JavaMail (and activation.jar). [if using the Web Services]  JavaMail can be downloaded at:
	http://java.sun.com/products/javamail/
	http://java.sun.com/products/javabeans/glasgow/jaf.html

5. DEPLOYMENT

* Servlet Container that can handle JSP 2.0 (i.e. Tomcat 5.0.28)

