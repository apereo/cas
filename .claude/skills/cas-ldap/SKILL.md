---
name: cas-ldap
description: "Review and change LDAP integration: LdapUtils, connection factories and pools, search result handling, password transport, fail-open authorization, logging of directory data. Use when touching LDAP authentication, attribute repositories, password management or any LDAP-backed module."
---

# LDAP

## LDAP review discipline

- Start at the shared seam. `LdapUtils` builds connection configs, filters, authenticators, DN/entry resolvers
  and person-attribute DAOs for every LDAP-backed feature; `LdapConnectionFactory` wraps every search, modify,
  add, delete and password-modify. Findings there generalize; findings in one feature module usually do not.
- Follow the whole flow: property model -> `LdapUtils` construction -> auto-configuration bean -> the calling
  repository or handler -> the webflow/endpoint boundary that consumes the result. Most real defects live in
  the mismatch between two of those layers, not inside one class.
- Search results are a trust boundary. Check the LDAP result code, not just whether an entry came back, and
  check what happens on truncation (server size/time/admin limits), on referral or continuation references,
  and on multi-valued attributes that directories return in ranges.
- Connection factories are pooled and stateful. Confirm they are created once as beans, that a pool used for
  user binds is passivated back to its original bind state, and that maps of factories are keyed by something
  that actually distinguishes two configurations.
- For any code that reads or writes a password, verify the transport requirement against the current RFC text
  and confirm the guard is enforcement rather than a log line; read the boolean carefully for inverted sense.
- Treat a fail-open authorization branch as critical regardless of how it is reached. Trace the default value
  of every property the branch depends on, including Spring Boot defaults CAS does not override.
- Watch for values that are written by one path and compared by another, and for filter/template parameters
  that are silently ignored when the operator's template does not reference them.
- Secrets and directory internals must not reach logs or released attributes: password-reset answers, bind
  credentials, diagnostic messages and matched DNs all deserve a second look.
