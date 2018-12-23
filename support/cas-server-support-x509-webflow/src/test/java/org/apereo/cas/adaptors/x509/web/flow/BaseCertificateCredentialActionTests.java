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
@TestPropertySource(locations = {"classpath:/x509.properties"})
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
