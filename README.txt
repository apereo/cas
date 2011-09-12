CENTRAL AUTHENTICATION SERVICE (CAS)
--------------------------------------------------------------------
http://www.ja-sig.org/products/cas/

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

CAS requires J2SE 1.5 and J2EE1.3.

3. DISTRIBUTION JAR FILES

The "modules" directory contains distinct jars for each Maven module.  It also
contains a war file that can be used for demo purposes.

4.   DEPENDENCIES

5. DEPLOYMENT

* Servlet Container that can handle JSP 2.0 (i.e. Tomcat 5.0.28)

6. NOTE
If building CAS from the source, running the test cases currently requires an active Internet connection.
Please see: http://maven.apache.org/general.html#skip-test on how to disable the tests.


