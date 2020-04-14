package org.apereo.cas.support.oauth.web.response.accesstoken;

import org.apereo.cas.AbstractOAuth20Tests;
import org.apereo.cas.services.DefaultRegisteredServiceProperty;
import org.apereo.cas.services.RegisteredServiceProperty;
import org.apereo.cas.support.oauth.OAuth20Constants;

import com.nimbusds.jwt.JWTParser;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.ParseException;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link OAuth20DefaultAccessTokenResponseGeneratorTests}.
 *
 * @author Misagh Moayyed
 * @since 6.1.0
 */
@Tag("OAuth")
public class OAuth20DefaultAccessTokenResponseGeneratorTests extends AbstractOAuth20Tests {

    @BeforeEach
    public void initialize() {
        clearAllServices();
    }

    @Test
    public void verifyAccessTokenAsDefault() {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(false);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        val model = mv.getModel();
        assertTrue(model.containsKey(OAuth20Constants.ACCESS_TOKEN));
        assertTrue(model.containsKey(OAuth20Constants.EXPIRES_IN));
        assertTrue(model.containsKey(OAuth20Constants.SCOPE));
        assertTrue(model.containsKey(OAuth20Constants.TOKEN_TYPE));

        assertThrows(ParseException.class, () -> {
            val at = model.get(OAuth20Constants.ACCESS_TOKEN).toString();
            JWTParser.parse(at);
        });
    }

    @Test
    public void verifyAccessTokenAsJwt() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);
        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

    @Test
    public void verifyAccessTokenAsJwtPerService() throws Exception {
        val registeredService = getRegisteredService("example", "secret", new LinkedHashSet<>());
        registeredService.setJwtAccessToken(true);

        val signingKey = new DefaultRegisteredServiceProperty();
        signingKey.addValue("pR3Vizkn5FSY5xCg84cIS4m-b6jomamZD68C8ash-TlNmgGPcoLgbgquxHPoi24tRmGpqHgM4mEykctcQzZ-Xg");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_SIGNING_KEY.getPropertyName(), signingKey);

        val encKey = new DefaultRegisteredServiceProperty();
        encKey.addValue("0KVXaN-nlXafRUwgsr3H_l6hkufY7lzoTy7OVI5pN0E");
        registeredService.getProperties().put(
            RegisteredServiceProperty.RegisteredServiceProperties.ACCESS_TOKEN_AS_JWT_ENCRYPTION_KEY.getPropertyName(), encKey);

        servicesManager.save(registeredService);

        val mv = generateAccessTokenResponseAndGetModelAndView(registeredService);
        assertTrue(mv.getModel().containsKey(OAuth20Constants.ACCESS_TOKEN));

        val at = mv.getModel().get(OAuth20Constants.ACCESS_TOKEN).toString();
        val jwt = JWTParser.parse(at);
        assertNotNull(jwt);
    }

}
