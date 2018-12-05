package org.apereo.cas.util;

import lombok.Setter;
import lombok.val;
import org.apache.commons.lang3.ArrayUtils;
import org.ldaptive.SearchRequest;
import org.ldaptive.auth.AuthenticationCriteria;
import org.ldaptive.auth.PooledSearchEntryResolver;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link BinaryAttributeAwarePooledSearchEntryResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Setter
public class BinaryAttributeAwarePooledSearchEntryResolver extends PooledSearchEntryResolver {
    private List<String> binaryAttributes = new ArrayList<>();

    @Override
    protected SearchRequest createSearchRequest(final AuthenticationCriteria ac) {
        val request = super.createSearchRequest(ac);
        request.setBinaryAttributes(binaryAttributes.toArray(ArrayUtils.EMPTY_STRING_ARRAY));
        return request;
    }
}
