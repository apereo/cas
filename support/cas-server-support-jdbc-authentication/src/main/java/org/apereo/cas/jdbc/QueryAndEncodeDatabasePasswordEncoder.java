package org.apereo.cas.jdbc;

import module java.base;
import org.apereo.cas.configuration.model.support.jdbc.authn.QueryEncodeJdbcAuthenticationProperties;
import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;

/**
 * This is {@link QueryAndEncodeDatabasePasswordEncoder}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RequiredArgsConstructor
public class QueryAndEncodeDatabasePasswordEncoder implements DatabasePasswordEncoder {
    protected final QueryEncodeJdbcAuthenticationProperties properties;

    @Override
    public String encode(final String password, final Map<String, Object> queryValues) {
        val iterations = getIterations(queryValues);
        val dynaSalt = getDynamicSalt(queryValues);
        val staticSalt = getStaticSalt(queryValues);
        return DigestUtils.rawDigest(properties.getAlgorithmName(), staticSalt, dynaSalt, password, iterations);
    }

    protected int getIterations(final Map<String, Object> queryValues) {
        var iterations = properties.getNumberOfIterations();
        if (queryValues.containsKey(properties.getNumberOfIterationsFieldName())) {
            val longAsStr = queryValues.get(properties.getNumberOfIterationsFieldName()).toString();
            iterations = Integer.parseInt(longAsStr);
        }
        return iterations;
    }

    protected byte[] getStaticSalt(final Map<String, Object> queryValues) {
        return FunctionUtils.doIfNotBlank(properties.getStaticSalt(),
            () -> properties.getStaticSalt().getBytes(StandardCharsets.UTF_8),
            () -> ArrayUtils.EMPTY_BYTE_ARRAY);
    }

    protected byte[] getDynamicSalt(final Map<String, Object> queryValues) {
        return queryValues.containsKey(properties.getSaltFieldName())
            ? queryValues.get(properties.getSaltFieldName()).toString().getBytes(StandardCharsets.UTF_8)
            : ArrayUtils.EMPTY_BYTE_ARRAY;
    }
}
