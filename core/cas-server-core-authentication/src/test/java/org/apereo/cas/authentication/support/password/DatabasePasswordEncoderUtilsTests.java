package org.apereo.cas.authentication.support.password;

import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.jdbc.DatabasePasswordEncoder;
import org.apereo.cas.jdbc.DatabasePasswordEncoderUtils;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link DatabasePasswordEncoderUtilsTests}.
 *
 * @author Yusuf Gunduz
 *
 * @since 7.3.0
 */

@Tag("Utility")
class DatabasePasswordEncoderUtilsTests {

    private static void verifyEncoder(final DatabasePasswordEncoder encoder) {
        assertNotNull(encoder);
        assertInstanceOf(DatabasePasswordEncoder.class, encoder);
    }

    @Test
    void verifyNoType() {
        val properties = new QueryEncodeJdbcAuthenticationProperties();
        properties.setDatabasePasswordEncoder(null);
        var encoder = DatabasePasswordEncoderUtils.newDatabasePasswordEncoder(properties);
        assertNotNull(encoder);
        properties.setDatabasePasswordEncoder(StringUtils.EMPTY);
        encoder = DatabasePasswordEncoderUtils.newDatabasePasswordEncoder(properties);
        verifyEncoder(encoder);
    }

    @Test
    void verifyClassType() {
        val properties = new QueryEncodeJdbcAuthenticationProperties();
        properties.setDatabasePasswordEncoder("org.example.cas.SampleDatabasePasswordEncoder");
        val encoder = DatabasePasswordEncoderUtils.newDatabasePasswordEncoder(properties);
        verifyEncoder(encoder);
    }
}
