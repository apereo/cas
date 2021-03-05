package org.apereo.cas.web.flow;

import org.apereo.cas.AbstractCentralAuthenticationServiceTests;
import org.apereo.cas.BaseCasCoreTests;
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
import org.apereo.cas.web.flow.config.TokenAuthenticationWebflowConfiguration;
import org.apereo.cas.web.support.WebUtils;

import com.nimbusds.jose.EncryptionMethod;
import com.nimbusds.jose.JWEAlgorithm;
import com.nimbusds.jose.JWSAlgorithm;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.pac4j.core.profile.CommonProfile;
import org.pac4j.jwt.config.encryption.SecretEncryptionConfiguration;
import org.pac4j.jwt.config.signature.SecretSignatureConfiguration;
import org.pac4j.jwt.profile.JwtGenerator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.springframework.webflow.context.servlet.ServletExternalContext;
import org.springframework.webflow.execution.Action;
import org.springframework.webflow.test.MockRequestContext;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link TokenAuthenticationActionTests}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@SpringBootTest(classes = {
    BaseCasCoreTests.SharedTestConfiguration.class,
    TokenAuthenticationConfiguration.class,
    TokenAuthenticationWebflowConfiguration.class
})
@Tag("WebflowActions")
public class TokenAuthenticationActionTests extends AbstractCentralAuthenticationServiceTests {
    private static final RandomStringGenerator RANDOM_STRING_GENERATOR = new DefaultRandomStringGenerator();

    private static final String SIGNING_SECRET = RANDOM_STRING_GENERATOR.getNewString(256);

    private static final String ENCRYPTION_SECRET = RANDOM_STRING_GENERATOR.getNewString(48);

    @Autowired
    @Qualifier("tokenAuthenticationAction")
    private ObjectProvider<Action> action;

    @Autowired
    @Qualifier("servicesManager")
    private ObjectProvider<ServicesManager> servicesManager;

    @BeforeEach
    public void before() {
        val svc = RegisteredServiceTestUtils.getRegisteredService("https://example.token.org");
        svc.setAttributeReleasePolicy(new ReturnAllAttributeReleasePolicy());
        val prop = new DefaultRegisteredServiceProperty();
        prop.addValue(SIGNING_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_SIGNING.getPropertyName(), prop);

        val prop2 = new DefaultRegisteredServiceProperty();
        prop2.addValue(ENCRYPTION_SECRET);
        svc.getProperties().put(RegisteredServiceProperty.RegisteredServiceProperties.TOKEN_SECRET_ENCRYPTION.getPropertyName(), prop2);
        this.servicesManager.getObject().save(svc);
    }

    @Test
    public void verifyAction() throws Exception {
        val g = new JwtGenerator();

        g.setSignatureConfiguration(new SecretSignatureConfiguration(SIGNING_SECRET, JWSAlgorithm.HS256));
        g.setEncryptionConfiguration(new SecretEncryptionConfiguration(ENCRYPTION_SECRET, JWEAlgorithm.DIR, EncryptionMethod.A192CBC_HS384));

        val profile = new CommonProfile();
        profile.setId("casuser");
        profile.addAttribute("uid", "uid");
        profile.addAttribute("givenName", "CASUser");
        profile.addAttribute("memberOf", CollectionUtils.wrapSet("system", "cas", "admin"));
        val token = g.generate(profile);

        val request = new MockHttpServletRequest();
        request.addHeader(TokenConstants.PARAMETER_NAME_TOKEN, token);
        val context = new MockRequestContext();
        context.setExternalContext(new ServletExternalContext(new MockServletContext(), request, new MockHttpServletResponse()));
        WebUtils.putServiceIntoFlowScope(context, RegisteredServiceTestUtils.getService("https://example.token.org"));
        assertEquals(CasWebflowConstants.TRANSITION_ID_SUCCESS, this.action.getObject().execute(context).getId());
    }
}
