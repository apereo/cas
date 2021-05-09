package org.apereo.cas.adaptors.x509.authentication.ldap;

import org.apereo.cas.adaptors.x509.BaseX509Tests;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * This is {@link BaseX509LdapResourceFetcherTests}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@SpringBootTest(classes = BaseX509Tests.SharedTestConfiguration.class,
    properties = {
        "cas.authn.policy.any.try-all=true",

        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
        "cas.authn.attribute-repository.stub.attributes.certificateRevocationList=certificateRevocationList",

        "cas.authn.x509.principal-type=SERIAL_NO_DN",
        "cas.authn.x509.reg-ex-trusted-issuer-dn-pattern=CN=\\\\w+,DC=jasig,DC=org",
        "cas.authn.x509.crl-fetcher=ldap",

        "cas.authn.x509.ldap.ldap-url=ldap://localhost:" + BaseX509LdapResourceFetcherTests.LDAP_PORT,
        "cas.authn.x509.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.x509.ldap.search-filter=cn=X509",
        "cas.authn.x509.ldap.bind-dn=cn=Directory Manager",
        "cas.authn.x509.ldap.bind-credential=password"
    })
@EnableScheduling
public abstract class BaseX509LdapResourceFetcherTests {
    protected static final int LDAP_PORT = 10389;
}
