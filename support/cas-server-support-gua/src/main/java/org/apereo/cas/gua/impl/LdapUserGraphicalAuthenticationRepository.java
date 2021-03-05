package org.apereo.cas.gua.impl;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.LdapUtils;

import com.google.common.io.ByteSource;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.ldaptive.ConnectionFactory;
import org.ldaptive.ReturnAttributes;
import org.ldaptive.SearchResponse;
import org.springframework.beans.factory.DisposableBean;

/**
 * This is {@link LdapUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
public class LdapUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository, DisposableBean {
    private static final long serialVersionUID = 421732017215881244L;

    private final CasConfigurationProperties casProperties;

    private final ConnectionFactory connectionFactory;

    @Override
    public void destroy() {
        this.connectionFactory.close();
    }

    @Override
    public ByteSource getGraphics(final String username) {
        val gua = casProperties.getAuthn().getGua();
        val response = searchForId(username);
        if (LdapUtils.containsResultEntry(response)) {
            val entry = response.getEntry();
            val attribute = entry.getAttribute(gua.getLdap().getImageAttribute());
            if (attribute != null && attribute.isBinary()) {
                return ByteSource.wrap(attribute.getBinaryValue());
            }
        }
        return ByteSource.empty();
    }

    @SneakyThrows
    private SearchResponse searchForId(final String id) {
        val gua = casProperties.getAuthn().getGua();
        val filter = LdapUtils.newLdaptiveSearchFilter(gua.getLdap().getSearchFilter(),
            LdapUtils.LDAP_SEARCH_FILTER_DEFAULT_PARAM_NAME,
            CollectionUtils.wrap(id));
        return LdapUtils.executeSearchOperation(
            this.connectionFactory,
            gua.getLdap().getBaseDn(),
            filter,
            gua.getLdap().getPageSize(),
            new String[]{gua.getLdap().getImageAttribute()},
            ReturnAttributes.ALL_USER.value());
    }

}
