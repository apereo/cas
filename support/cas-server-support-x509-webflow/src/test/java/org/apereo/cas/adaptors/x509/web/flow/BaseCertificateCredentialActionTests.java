package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.principal.AbstractX509CertificateTests;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.web.extractcert.X509CertificateExtractorConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseCertificateCredentialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@TestPropertySource(properties = {
    "ldap.managerDn=cn=Directory Manager,dc=example,dc=org",
    "ldap.managerPassword=Password",
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
    "cas.authn.x509.ldap.baseDn=ou=people,dc=example,dc=org",
    "cas.authn.x509.ldap.searchFilter=cn=X509",
    "cas.authn.x509.ldap.bindDn=${ldap.managerDn}",
    "cas.authn.x509.ldap.bindCredential=${ldap.managerPassword}"
})
@Import(value = {
    X509AuthenticationWebflowConfiguration.class,
    X509AuthenticationConfiguration.class,
    X509CertificateExtractorConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class
})
public abstract class BaseCertificateCredentialActionTests extends AbstractX509CertificateTests {
    @Autowired
    @Qualifier("x509Check")
    protected ObjectProvider<Action> action;
}
