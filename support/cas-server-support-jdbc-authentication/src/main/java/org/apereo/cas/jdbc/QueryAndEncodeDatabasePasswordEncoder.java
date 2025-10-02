package org.apereo.cas.jdbc;

import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * This is {@link QueryAndEncodeDatabasePasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
public class QueryAndEncodeDatabasePasswordEncoder extends AbstractDatabasePasswordEncoder {

    public QueryAndEncodeDatabasePasswordEncoder(
        QueryEncodeJdbcAuthenticationProperties properties) {
        super(properties);
    }
}
