package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.adaptors.x509.config.X509AuthenticationConfiguration;
import org.apereo.cas.adaptors.x509.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationHandlersConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationMetadataConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPolicyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationPrincipalConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationServiceSelectionStrategyConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationSupportConfiguration;
import org.apereo.cas.config.CasCoreConfiguration;
import org.apereo.cas.config.CasCoreHttpConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreServicesConfiguration;
import org.apereo.cas.config.CasCoreTicketCatalogConfiguration;
import org.apereo.cas.config.CasCoreTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasCoreTicketsConfiguration;
import org.apereo.cas.config.CasCoreUtilConfiguration;
import org.apereo.cas.config.CasCoreWebConfiguration;
import org.apereo.cas.config.CasDefaultServiceTicketIdGeneratorsConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.support.CasWebApplicationServiceFactoryConfiguration;
import org.apereo.cas.logout.config.CasCoreLogoutConfiguration;
import org.apereo.cas.validation.config.CasCoreValidationConfiguration;
import org.apereo.cas.web.config.CasCookieConfiguration;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasMultifactorAuthenticationWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.flow.config.X509AuthenticationWebflowConfiguration;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseCertificateCredentialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    MailSenderAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    CasCoreServicesConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    AbstractCentralAuthenticationServiceTests.CasTestConfiguration.class,
    CasCoreServicesAuthenticationConfiguration.class,
    CasWebApplicationServiceFactoryConfiguration.class,
    CasDefaultServiceTicketIdGeneratorsConfiguration.class,
    CasCoreTicketIdGeneratorsConfiguration.class,
    CasCoreUtilConfiguration.class,
    CasCoreAuthenticationConfiguration.class,
    CasCoreAuthenticationPrincipalConfiguration.class,
    CasCoreAuthenticationPolicyConfiguration.class,
    CasCoreAuthenticationMetadataConfiguration.class,
    CasCoreAuthenticationSupportConfiguration.class,
    CasCoreAuthenticationHandlersConfiguration.class,
    CasCoreHttpConfiguration.class,
    CasCoreConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreAuthenticationServiceSelectionStrategyConfiguration.class,
    CasCoreTicketsConfiguration.class,
    CasCoreTicketCatalogConfiguration.class,
    CasCoreWebConfiguration.class,
    CasWebflowContextConfiguration.class,
    CasCoreWebflowConfiguration.class,
    CasCoreLogoutConfiguration.class,
    CasCookieConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreValidationConfiguration.class,
    X509AuthenticationWebflowConfiguration.class,
    X509AuthenticationConfiguration.class,
    X509CertificateExtractorConfiguration.class
},
    properties = {
        "cas.authn.attributeRepository.stub.attributes.uid=uid",
        "cas.authn.attributeRepository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attributeRepository.stub.attributes.groupMembership=adopters",
        "cas.authn.attributeRepository.stub.attributes.certificateRevocationList=certificateRevocationList",
        "cas.authn.x509.regExTrustedIssuerDnPattern=CN=\\\\w+,DC=jasig,DC=org",
        "cas.authn.x509.principalType=SERIAL_NO_DN",
        "cas.authn.policy.any.tryAll=true",
        "cas.authn.x509.crlFetcher=ldap",
        "cas.authn.x509.ldap.ldap-url=ldap://localhost:1389",
        "cas.authn.x509.ldap.baseDn=ou=people,dc=example,dc=org",
        "cas.authn.x509.ldap.searchFilter=cn=X509",
        "cas.authn.x509.ldap.bindDn=cn=Directory Manager,dc=example,dc=org",
        "cas.authn.x509.ldap.bindCredential=Password"
    })
public abstract class BaseCertificateCredentialActionTests {
    public static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    @Autowired
    @Qualifier("x509Check")
    protected ObjectProvider<Action> action;
}
