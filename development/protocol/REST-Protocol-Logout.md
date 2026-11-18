# Logout - REST Protocol

Source: https://apereo.github.io/cas/development/protocol/REST-Protocol-Logout.html

End a single sign-on session through the REST API by deleting its ticket-granting ticket.

Destroy the SSO session by removing the issued ticket:

```bash
DELETE /cas/v1/tickets/TGT-fdsjfsdfjkalfewrihfdhfaie HTTP/1.0
```
