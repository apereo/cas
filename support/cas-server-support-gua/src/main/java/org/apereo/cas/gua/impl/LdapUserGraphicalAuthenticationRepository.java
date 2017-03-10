package org.apereo.cas.gua.impl;

import com.google.common.io.ByteSource;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.configuration.support.Beans;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.util.LdapUtils;
import org.ldaptive.LdapAttribute;
import org.ldaptive.LdapEntry;
import org.ldaptive.LdapException;
import org.ldaptive.Response;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchFilter;
import org.ldaptive.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;

/**
 * This is {@link LdapUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class LdapUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository {
    private static final long serialVersionUID = 421732017215881244L;
    private static final Logger LOGGER = LoggerFactory.getLogger(LdapUserGraphicalAuthenticationRepository.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Override
    public ByteSource getGraphics(final String username) {
        try {
            final GraphicalUserAuthenticationProperties gua = casProperties.getAuthn().getGua();
            final Response<SearchResult> response = searchForId(username);
            if (LdapUtils.containsResultEntry(response)) {
                final LdapEntry entry = response.getResult().getEntry();
                final LdapAttribute attribute = entry.getAttribute(gua.getLdap().getImageAttribute());
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
        final GraphicalUserAuthenticationProperties gua = casProperties.getAuthn().getGua();
        final SearchFilter filter = Beans.newLdaptiveSearchFilter(gua.getLdap().getUserFilter(),
                Beans.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
                Arrays.asList(id));
        return LdapUtils.executeSearchOperation(
                Beans.newLdaptiveConnectionFactory(gua.getLdap()),
                gua.getLdap().getBaseDn(), filter,
                new String[]{gua.getLdap().getImageAttribute()},
                ReturnAttributes.NONE.value());
    }

}
