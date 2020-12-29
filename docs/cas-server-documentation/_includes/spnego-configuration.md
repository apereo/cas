```properties
# cas.authn.spnego.mixed-mode-authentication=false
# cas.authn.spnego.supported-browsers=MSIE,Trident,Firefox,AppleWebKit
# cas.authn.spnego.send401-on-authentication-failure=true
# cas.authn.spnego.ntlm-allowed=true
# cas.authn.spnego.principal-with-domain-name=false
# cas.authn.spnego.name=
# cas.authn.spnego.ntlm=false
```

{% include {{ version }}/persondirectory-configuration.md configKey="cas.authn.spnego.principal" %}

{% include {{ version }}/webflow-configuration.md configKey="cas.authn.spnego.webflow" %}

SPNEGO system settings can be configured as follows:

```properties
# cas.authn.spnego.system.kerberos-conf=
# cas.authn.spnego.system.login-conf=
# cas.authn.spnego.system.kerberos-realm=EXAMPLE.COM
# cas.authn.spnego.system.kerberos-debug=true
# cas.authn.spnego.system.use-subject-creds-only=false
# cas.authn.spnego.system.kerberos-kdc=172.10.1.10
```

SPNEGO authentication settings can be configured as follows:

```properties
# cas.authn.spnego.properties[0].cache-policy=600
# cas.authn.spnego.properties[0].jcifs-domain-controller=
# cas.authn.spnego.properties[0].jcifs-domain=
# cas.authn.spnego.properties[0].jcifs-password=
# cas.authn.spnego.properties[0].jcifs-username=
# cas.authn.spnego.properties[0].jcifs-service-password=
# cas.authn.spnego.properties[0].timeout=300000
# cas.authn.spnego.properties[0].jcifs-service-principal=HTTP/cas.example.com@EXAMPLE.COM
# cas.authn.spnego.properties[0].jcifs-netbios-wins=
```

SPNEGO client selection strategy can be configured as follows:

```properties
# cas.authn.spnego.host-name-client-action-strategy=hostnameSpnegoClientAction
```

SPNEGO client hostname selection can be configured as follows:

```properties
# cas.authn.spnego.alternative-remote-host-attribute=alternateRemoteHeader
# cas.authn.spnego.ips-to-check-pattern=127.+
# cas.authn.spnego.dns-timeout=2000
# cas.authn.spnego.host-name-pattern-string=.+
```

SPNEGO NTLM authentication can be configured as follows:

```properties
# cas.authn.ntlm.include-pattern=
# cas.authn.ntlm.load-balance=true
# cas.authn.ntlm.domain-controller=
# cas.authn.ntlm.name=
# cas.authn.ntlm.order=
# cas.authn.ntlm.enabled=false
```
