See http://www.ja-sig.org/wiki/display/CAS/CAS+Functional+Tests for the descrition of these tests.

DEPENDENCIES :
- Canoo Webtest 2.5
- The application proxyCallBackTest should be deployed twice an on a trusted Application Server
 over HTTPS

CONFIGURATION FILES : 
- properties\canoo.properties
- properties\local.properties : proxyCallBackURL1 and proxyCallBackURL2 should be mapped to
 proxyCallBackTest applications.

USAGE : 
- launch build.xml with ANT

