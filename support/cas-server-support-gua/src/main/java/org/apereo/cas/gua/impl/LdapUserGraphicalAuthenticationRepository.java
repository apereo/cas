package org.apereo.cas.gua.impl;

import com.google.common.io.ByteSource;
import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.CasConfigurationProperties;

import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is {@link LdapUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class LdapUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository {
    private static final long serialVersionUID = 421732017215881244L;


    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public ByteSource getGraphics(final String username) {
        try {
            final var gua = casProperties.getAuthn().getGua();
            final var response = searchForId(username);
            if (LdapUtils.containsResultEntry(response)) {
                final var entry = response.getResult().getEntry();
                final var attribute = entry.getAttribute(gua.getLdap().getImageAttribute());
                if (attribute != null && attribute.isBinary()) {
                    return ByteSource.wrap(attribute.getBinaryValue());
                }
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ByteSource.empty();
    }

    private Response<SearchResult> searchForId(final String id) throws LdapException {
        final var gua = casProperties.getAuthn().getGua();
        final var filter = LdapUtils.newLdaptiveSearchFilter(gua.getLdap().getSearchFilter(),
                LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                CollectionUtils.wrap(id));
        return LdapUtils.executeSearchOperation(
                LdapUtils.newLdaptiveConnectionFactory(gua.getLdap()),
                gua.getLdap().getBaseDn(), filter,
                new String[]{gua.getLdap().getImageAttribute()},
                ReturnAttributes.NONE.value());
    }

}
