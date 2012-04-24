The cas-server-support-oauth module is made to add support for OAuth in CAS server.
It allows two modes :

- CAS server can support OAuth protocol as an OAuth client : in this case, CAS authentication can be delegated to an OAuth provider like Facebook, GitHub, Google, LinkedIn, Twitter, Yahoo... or even an another CAS server using OAuth wrapper
- CAS server can support OAuth protocol as an OAuth server : in this case, CAS uses the OAuth wrapper and acts as an OAuth server, communicating through OAuth protocol version 2.0 with OAuth clients.

See documentation at : https://wiki.jasig.org/display/CASUM/OAuth
