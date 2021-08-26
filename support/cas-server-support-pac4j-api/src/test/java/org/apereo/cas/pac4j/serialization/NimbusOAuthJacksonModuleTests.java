package org.apereo.cas.pac4j.serialization;

import org.apereo.cas.util.RandomUtils;
import org.apereo.cas.util.serialization.AbstractJacksonBackedStringSerializer;

import com.nimbusds.oauth2.sdk.Scope;
import com.nimbusds.oauth2.sdk.pkce.CodeVerifier;
import com.nimbusds.oauth2.sdk.token.AccessTokenType;
import com.nimbusds.oauth2.sdk.token.BearerAccessToken;
import com.nimbusds.oauth2.sdk.token.RefreshToken;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link NimbusOAuthJacksonModuleTests}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
@Tag("Simple")
public class NimbusOAuthJacksonModuleTests {
    private static final AbstractJacksonBackedStringSerializer SERIALIZER = new AbstractJacksonBackedStringSerializer() {
        private static final long serialVersionUID = 1083978605633757365L;

        @Override
        public Class getTypeToSerialize() {
            return Object.class;
        }
    };

    @Test
    public void verifyOperation() throws Exception {
        val mapper = SERIALIZER.getObjectMapper();
        assertTrue(mapper.getRegisteredModuleIds().contains(NimbusOAuthJacksonModule.class.getName()));
        runTest(CodeVerifier.class, new CodeVerifier(RandomUtils.randomAlphabetic(CodeVerifier.MIN_LENGTH)));
        runTest(BearerAccessToken.class, new BearerAccessToken("access-token-value"));
        runTest(RefreshToken.class, new RefreshToken("access-token-value"));
        runTest(AccessTokenType.class, AccessTokenType.BEARER);
        runTest(Scope.class, new Scope("profile"));
    }

    private static void runTest(final Class clazz, final Object object) throws Exception {
        val mapper = SERIALIZER.getObjectMapper();
        val content = mapper.writeValueAsString(object);
        assertNotNull(mapper.readValue(content, clazz));
    }

}
