package org.apereo.cas.adaptors.x509.util;

import org.springframework.test.context.TestPropertySource;

/**
 * This is {@link X509TestProperties}.
 *
 * @author Timur Duehr
 * @since 6.1.0
 */
@TestPropertySource(properties = {
    "cas.authn.attributeRepository.stub.attributes.uid=uid",
    "cas.authn.attributeRepository.stub.attributes.eduPersonAffiliation=developer",
    "cas.authn.attributeRepository.stub.attributes.groupMembership=adopters",
    "cas.authn.attributeRepository.stub.attributes.certificateRevocationList=certificateRevocationList",
    "cas.authn.x509.regExTrustedIssuerDnPattern=CN=\\w+,DC=jasig,DC=org",
    "cas.authn.x509.principalType=SERIAL_NO_DN",
    "cas.authn.policy.any.tryAll=true",
    "cas.authn.x509.crlFetcher=ldap",
    "cas.authn.x509.ldap.ldapUrl=ldap://localhost:1389",
    "cas.authn.x509.ldap.useSsl=false",
    "cas.authn.x509.ldap.baseDn=${ldap.peopleDn}",
    "cas.authn.x509.ldap.searchFilter=cn=X509",
    "cas.authn.x509.ldap.bindDn=${ldap.managerDn}",
    "cas.authn.x509.ldap.bindCredential=${ldap.managerPassword}"
})
public interface X509TestProperties {
}
