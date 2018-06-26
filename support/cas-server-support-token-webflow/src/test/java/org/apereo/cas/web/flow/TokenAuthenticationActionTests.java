package org.apereo.cas.web.flow;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.authentication.CoreAuthenticationTestUtils;
import org.apereo.cas.config.TokenAuthenticationConfiguration;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceTestUtils;
import org.apereo.cas.services.ReturnAllAttributeReleasePolicy;
import org.apereo.cas.services.ServicesManager;
import org.apereo.cas.token.TokenConstants;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.gen.DefaultRandomStringGenerator;
import org.apereo.cas.util.gen.RandomStringGenerator;
import org.apereo.cas.web.flow.config.CasCoreWebflowConfiguration;
import org.apereo.cas.web.flow.config.CasWebflowContextConfiguration;
import org.apereo.cas.web.flow.config.TokenAuthenticationWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;
import org.junit.Test;
import org.junit.Before;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.Assert.*;

/**
 * This is {@link TokenAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Import({
    CasCoreWebflowConfiguration.class,
    CasWebflowContextConfiguration.class,
    TokenAuthenticationConfiguration.class,
    TokenAuthenticationWebflowConfiguration.class
})
@Slf4j
public class TokenAuthenticationActionTests extends AbstractCentralAuthenticationServiceTests {
    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new DefaultRandomStringGenerator();
    private static final String SIGNING_SECRET = RANDOM_STRING_GENERATOR.getNewString(256);
    private static final String ENCRYPTION_SECRET = RANDOM_STRING_GENERATOR.getNewString(48);

    @Autowired
    @Qualifier("tokenAuthenticationAction")
    private Action action;

    @Autowired
    @Qualifier("servicesManager")
    private ServicesManager servicesManager;

    @Before
    public void before() {
        final var svc = RegisteredServiceTestUtils.getRegisteredService("https://example.token.org");
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        var prop = new DefaultRegisteredServiceProperty();
        prop.addValue(SIGNING_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), prop);
        prop = new DefaultRegisteredServiceProperty();
        prop.addValue(ENCRYPTION_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), prop);
        this.servicesManager.save(svc);
    }

    @Test
    public void verifyAction() throws Exception {
        final JwtGenerator<CommonProfile> g = new JwtGenerator<>();

        g.setSignatureConfiguration(new SecretSignatureConfiguration(SIGNING_SECRET, JWSAlgorithm.HS256));
        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(ENCRYPTION_SECRET, JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384));

        final var profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttribute("uid", "uid");
        profile.addAttribute("givenName", "CASUser");
        profile.addAttribute("memberOf", CollectionUtils.wrapSet("system", "cas", "admin"));
        final var token = g.generate(profile);

        final var request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, token);
        final var context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putService(context, CoreAuthenticationTestUtils.getWebApplicationService("https://example.token.org"));
        assertEquals("success", this.action.execute(context).getId());
    }
}
