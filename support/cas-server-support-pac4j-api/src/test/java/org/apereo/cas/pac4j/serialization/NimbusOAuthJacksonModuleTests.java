package org.apereo.cas.pac4j.serialization;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.id.State;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.autoconfigure.RefreshAutoConfiguration;
import org.springframework.context.ConfigurableApplicationContext;
import java.io.Serial;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NimbusOAuthJacksonModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
@SpringBootTest(classes = RefreshAutoConfiguration.class)
@EnableConfigurationProperties(CasConfigurationProperties.class)
@ExtendWith(CasTestExtension.class)
class NimbusOAuthJacksonModuleTests {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Test
    void verifyOperation() {
        val serializer = new BaseJacksonSerializer(applicationContext, Object.class) {
            @Serial
            private static final long serialVersionUID = 1083978605633757365L;
        };

        val mapper = serializer.getObjectMapper();
        assertTrue(mapper.registeredModules().stream().anyMatch(module -> module.getModuleName().contains(NimbusOAuthJacksonModule.class.getName())));
        val codeVerifier = new CodeVerifier(RandomUtils.randomAlphabetic(CodeVerifier.MIN_LENGTH));
        runTest(serializer, CodeVerifier.class, codeVerifier);
        val accessToken = new BearerAccessToken("access-token-value");
        runTest(serializer, BearerAccessToken.class, accessToken);
        val refreshToken = new RefreshToken("refresh-token-value");
        runTest(serializer, RefreshToken.class, refreshToken);
        runTest(serializer, AccessTokenType.class, AccessTokenType.BEARER);
        val scope = new Scope("profile");
        runTest(serializer, Scope.class, scope);
        val state = new State("d2b9f54546");
        runTest(serializer, State.class, state);

        val container = new Container(codeVerifier, accessToken, refreshToken, AccessTokenType.BEARER, scope, state);
        runTest(serializer, Container.class, container);
    }

    private static void runTest(final BaseJacksonSerializer serializer, final Class clazz, final Object object) {
        val mapper = serializer.getObjectMapper();
        val content = mapper.writeValueAsString(object);
        val result = mapper.readValue(content, clazz);
        assertNotNull(result);
        assertEquals(object, result);
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
    @NoArgsConstructor
    @AllArgsConstructor
    @Getter
    @Setter
    @EqualsAndHashCode
    private static final class Container {
        private CodeVerifier codeVerifier;
        private BearerAccessToken bearerAccessToken;
        private RefreshToken refreshToken;
        private AccessTokenType accessTokenType;
        private Scope scope;
        private State state;
    }
}
