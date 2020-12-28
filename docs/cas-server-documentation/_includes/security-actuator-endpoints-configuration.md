Global endpoint security configuration activated by CAS may be
controlled under the configuration key `cas.monitor.endpoints.endpoint.{{ include.configKey }}`.

There is a special endpoint named `defaults`  which serves as a
shortcut that controls the security of all endpoints, if left undefined in CAS settings. 

Accessing an endpoint over the web can be allowed via a special login form whose access and presence can be controlled via:

```properties
# cas.monitor.endpoints.form-login-enabled=false
``` 

Note that any individual endpoint must be first enabled before any security 
can be applied. The security of all endpoints is controlled using the following settings:

```properties
# cas.monitor.endpoints.endpoint.{{ include.configKey }}.required-roles[0]=
# cas.monitor.endpoints.endpoint.{{ include.configKey }}.required-authorities[0]=
# cas.monitor.endpoints.endpoint.{{ include.configKey }}.required-ip-addresses[0]=
# cas.monitor.endpoints.endpoint.{{ include.configKey }}.access[0]=PERMIT|ANONYMOUS|DENY|AUTHENTICATED|ROLE|AUTHORITY|IP_ADDRESS
```

The following access levels are allowed for each individual endpoint:

| Type                    | Description
|-------------------------|----------------------------------------------------------------
| `PERMIT`                | Allow open access to the endpoint.
| `ANONYMOUS`             | Allow anonymous access to the endpoint.
| `DENY`                  | Default. Block access to the endpoint.
| `AUTHENTICATED`         | Require authenticated access to the endpoint.
| `ROLE`                  | Require authenticated access to the endpoint along with a role requirement.
| `AUTHORITY`             | Require authenticated access to the endpoint along with an authority requirement.
| `IP_ADDRESS`            | Require authenticated access to the endpoint using a collection of IP addresses.
