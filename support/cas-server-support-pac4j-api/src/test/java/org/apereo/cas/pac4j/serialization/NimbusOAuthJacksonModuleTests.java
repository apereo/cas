package org.apereo.cas.pac4j.serialization;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.test.CasTestExtension;
import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.BaseJacksonSerializer;
import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
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
    void verifyOperation() throws Throwable {
        val serializer = new BaseJacksonSerializer(applicationContext, Object.class) {
            @Serial
            private static final long serialVersionUID = 1083978605633757365L;
        };

        val mapper = serializer.getObjectMapper();
        assertTrue(mapper.getRegisteredModuleIds().contains(NimbusOAuthJacksonModule.class.getName()));
        runTest(serializer, CodeVerifier.class, new CodeVerifier(RandomUtils.randomAlphabetic(CodeVerifier.MIN_LENGTH)));
        runTest(serializer, BearerAccessToken.class, new BearerAccessToken("access-token-value"));
        runTest(serializer, RefreshToken.class, new RefreshToken("access-token-value"));
        runTest(serializer, AccessTokenType.class, AccessTokenType.BEARER);
        runTest(serializer, Scope.class, new Scope("profile"));
    }

    private static void runTest(final BaseJacksonSerializer serializer, final Class clazz, final Object object) throws Exception {
        val mapper = serializer.getObjectMapper();
        val content = mapper.writeValueAsString(object);
        assertNotNull(mapper.readValue(content, clazz));
    }

}
