package org.apereo.cas.jdbc;

import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import lombok.val;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link QueryAndEncodeDatabasePasswordEncoderTests}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@Tag("JDBCAuthentication")
class QueryAndEncodeDatabasePasswordEncoderTests {
    private static final String STATIC_SALT = "oU92&p4b&NIWjAbC";
    private static final String DYNAMIC_SALT = "l!Ue%wj6TPMNf*lI";

    @Test
    void verifyStaticSaltOnly() {
        val properties = new QueryEncodeJdbcAuthenticationProperties()
            .setAlgorithmName("SHA256")
            .setStaticSalt(STATIC_SALT)
            .setNumberOfIterations(1);
        val encoder = new QueryAndEncodeDatabasePasswordEncoder(properties);
        val encoded = encoder.encode("p@$$w0rd", Map.of());
        assertEquals("4c32a1b1d6fa058a3e145c474bebc7dc30a34ad40df18202006e30d9313b3918", encoded);
    }

    @Test
    void verifyStaticAndDynamicSalt() {
        val properties = new QueryEncodeJdbcAuthenticationProperties()
            .setAlgorithmName("SHA512")
            .setStaticSalt(STATIC_SALT)
            .setNumberOfIterations(100)
            .setSaltFieldName("dynamicSalt");
         val encoder = new QueryAndEncodeDatabasePasswordEncoder(properties);
         val encoded = encoder.encode("p@$$w0rd", Map.of("dynamicSalt", DYNAMIC_SALT));
         assertEquals("51072005fd3ecfc1edc22fc1eff611487defde788301991a667119ae32cecc7da1af6afed420b61318aa11151b0f6ec357c1869dc75acccdb5170273fad4e957", encoded);
    }

    @Test
    void verifyNoStaticSaltWithDynamicIterations() {
        val properties = new QueryEncodeJdbcAuthenticationProperties()
            .setAlgorithmName("SHA256")
            .setNumberOfIterationsFieldName("iterations")
            .setSaltFieldName("dynamicSalt");
        val encoder = new QueryAndEncodeDatabasePasswordEncoder(properties);
        val encoded = encoder.encode("p@$$w0rd", Map.of("dynamicSalt", DYNAMIC_SALT, "iterations", 1000));
        assertEquals("d14889d4eec75142f69f3620a629e6efa79ac92d11ef9214ee0dd4ddf7c0108c", encoded);
    }
}
