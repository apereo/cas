package org.apereo.cas.adaptors.x509.web.flow;

import org.apereo.cas.adaptors.x509.authentication.CasX509Certificate;
import org.apereo.cas.config.CasAuthenticationEventExecutionPlanTestConfiguration;
import org.apereo.cas.config.CasCoreAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreAutoConfiguration;
import org.apereo.cas.config.CasCoreCookieAutoConfiguration;
import org.apereo.cas.config.CasCoreLogoutAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationAutoConfiguration;
import org.apereo.cas.config.CasCoreMultifactorAuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasCoreNotificationsAutoConfiguration;
import org.apereo.cas.config.CasCoreServicesAutoConfiguration;
import org.apereo.cas.config.CasCoreTicketsAutoConfiguration;
import org.apereo.cas.config.CasCoreUtilAutoConfiguration;
import org.apereo.cas.config.CasCoreValidationAutoConfiguration;
import org.apereo.cas.config.CasCoreWebAutoConfiguration;
import org.apereo.cas.config.CasCoreWebflowAutoConfiguration;
import org.apereo.cas.config.CasPersonDirectoryAutoConfiguration;
import org.apereo.cas.config.CasRegisteredServicesTestConfiguration;
import org.apereo.cas.config.CasX509AuthenticationAutoConfiguration;
import org.apereo.cas.config.CasX509AuthenticationWebflowAutoConfiguration;
import org.apereo.cas.config.CasX509CertificateExtractorAutoConfiguration;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.spring.boot.SpringBootTestAutoConfigurations;
import org.apereo.cas.web.flow.CasWebflowConstants;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.webflow.execution.Action;

/**
 * This is {@link BaseCertificateCredentialActionTests}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@SpringBootTestAutoConfigurations
@SpringBootTest(classes = {
    CasAuthenticationEventExecutionPlanTestConfiguration.class,
    CasCoreServicesAutoConfiguration.class,
    CasCoreUtilAutoConfiguration.class,
    CasCoreAuthenticationAutoConfiguration.class,
    CasCoreAutoConfiguration.class,
    CasRegisteredServicesTestConfiguration.class,
    CasCoreTicketsAutoConfiguration.class,
    CasCoreWebAutoConfiguration.class,
    CasCoreWebflowAutoConfiguration.class,
    CasCoreLogoutAutoConfiguration.class,
    CasCoreCookieAutoConfiguration.class,
    CasCoreMultifactorAuthenticationAutoConfiguration.class,
    CasCoreMultifactorAuthenticationWebflowAutoConfiguration.class,
    CasPersonDirectoryAutoConfiguration.class,
    CasCoreValidationAutoConfiguration.class,
    CasCoreNotificationsAutoConfiguration.class,
    CasX509AuthenticationWebflowAutoConfiguration.class,
    CasX509AuthenticationAutoConfiguration.class,
    CasX509CertificateExtractorAutoConfiguration.class
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
@ExtendWith(CasTestExtension.class)
public abstract class BaseCertificateCredentialActionTests {
    public static final CasX509Certificate VALID_CERTIFICATE = new CasX509Certificate(true);

    @Autowired
    @Qualifier(CasWebflowConstants.ACTION_ID_X509_CHECK)
    protected Action action;

    @Autowired
    protected ConfigurableApplicationContext applicationContext;

}
