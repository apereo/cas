package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryTestConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasWebflowAutoConfiguration;
import org.apereo.cas.config.X509AuthenticationConfiguration;
import org.apereo.cas.config.X509AuthenticationWebflowConfiguration;
import org.apereo.cas.config.X509CertificateExtractorConfiguration;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.web.servlet.WebMvcAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseCertificateCredentialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTest(classes = {
    AopAutoConfiguration.class,
    RefreshAutoConfiguration.class,
    WebMvcAutoConfiguration.class,
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasWebflowAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCookieAutoConfiguration.class,
    CasCoreMultifactorAuthenticationConfiguration.class,
    CasMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasPersonDirectoryTestConfiguration.class,
    CasCoreValidationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    X509AuthenticationWebflowConfiguration.class,
    X509AuthenticationConfiguration.class,
    X509CertificateExtractorConfiguration.class
},
    properties = {
        "cas.authn.attribute-repository.stub.attributes.uid=uid",
        "cas.authn.attribute-repository.stub.attributes.eduPersonAffiliation=developer",
        "cas.authn.attribute-repository.stub.attributes.groupMembership=adopters",
        "cas.authn.attribute-repository.stub.attributes.certificateRevocationList=certificateRevocationList",
        "cas.authn.x509.reg-ex-trusted-issuer-dn-pattern=CN=\\\\w+,DC=jasig,DC=org",
        "cas.authn.x509.principal-type=SERIAL_NO_DN",
        "cas.authn.policy.any.try-all=true",
        "cas.authn.x509.crl-fetcher=ldap",
        "cas.authn.x509.ldap.ldap-url=ldap://localhost:1389",
        "cas.authn.x509.ldap.base-dn=ou=people,dc=example,dc=org",
        "cas.authn.x509.ldap.search-filter=cn=X509",
        "cas.authn.x509.ldap.bind-dn=cn=Directory Manager,dc=example,dc=org",
        "cas.authn.x509.ldap.bind-credential=Password"
    })
public abstract class BaseCertificateCredentialActionTests {
    public static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_X509_CHECK)
    protected Action action;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
