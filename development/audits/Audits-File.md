# File-based Audits

Source: https://apereo.github.io/cas/development/audits/Audits-File.html

File-based audit logs appear in a `cas_audit.log` file defined in the [Logging](https://apereo.github.io/cas/development/logging/Logging.html) configuration.

The following settings and properties are available from the CAS configuration catalog:

- `cas.audit.slf4j.auditable-fields` (Optional; type `String`; default `who,what,when,action,client_ip,server_ip,geo_location`): Control and define fields that can be accepted by the audit log. Accepted values are: - `who` - `what` - `action` - `application` - `when` - `user_agent` - `client_ip` - `server_ip` - `geo_location` - `headers`
- `cas.audit.slf4j.enabled` (Optional; type `Boolean`; default `true`): Decide whether Slf4j audits should be enabled.
- `cas.audit.slf4j.singleline-separator` (Optional; type `String`; default `|`): Character to separate audit fields if single-line audits are used.
- `cas.audit.slf4j.use-single-line` (Optional; type `Boolean`; default `false`): Indicates whether audit logs should be recorded as a single-line. By default, audit logs are split into multiple lines where each action and activity takes up a full line. This is a more compact version.

## Sample Log Output

```bash
WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: supplied credentials: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
ACTION: AUTHENTICATION_SUCCESS
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 EDT 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22

WHO: org.apereo.cas.support.oauth.authentication.principal.OAuthCredentials@6cd7c975
WHAT: TGT-9-qj2jZKQUmu1gQvXNf7tXQOJPOtROvOuvYAxybhZiVrdZ6pCUwW-cas01.example.org
ACTION: TICKET_GRANTING_TICKET_CREATED
APPLICATION: CAS
WHEN: Mon Aug 26 12:35:59 MST 2013
CLIENT IP ADDRESS: 172.16.5.181
SERVER IP ADDRESS: 192.168.200.22
```
