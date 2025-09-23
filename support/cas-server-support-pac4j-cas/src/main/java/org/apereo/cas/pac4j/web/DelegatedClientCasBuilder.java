package org.apereo.cas.pac4j.web;

import org.apereo.cas.authentication.CasSSLContext;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClient;
import org.apereo.cas.support.pac4j.authentication.clients.ConfigurableDelegatedClientBuilder;
import org.apereo.cas.web.flow.CasWebflowConfigurer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.pac4j.cas.client.CasClient;
import org.pac4j.cas.config.CasConfiguration;
import org.pac4j.cas.config.CasProtocol;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This is {@link DelegatedClientCasBuilder}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class DelegatedClientCasBuilder implements ConfigurableDelegatedClientBuilder {
    private static final Pattern PATTERN_LOGIN_URL = Pattern.compile('/' + CasWebflowConfigurer.FLOW_ID_LOGIN + '$');

    private final CasSSLContext casSSLContext;

    @Override
    public List<ConfigurableDelegatedClient> build(final CasConfigurationProperties casProperties) {
        return buildCasIdentityProviders(casProperties);
    }

    protected List<ConfigurableDelegatedClient> buildCasIdentityProviders(final CasConfigurationProperties casProperties) {
        val pac4jProperties = casProperties.getAuthn().getPac4j();
        return pac4jProperties
            .getCas()
            .stream()
            .filter(cas -> cas.isEnabled() && StringUtils.isNotBlank(cas.getLoginUrl()))
            .map(cas -> {
                val cfg = new CasConfiguration(cas.getLoginUrl(), CasProtocol.valueOf(cas.getProtocol()));
                val prefix = PATTERN_LOGIN_URL.matcher(cas.getLoginUrl()).replaceFirst("/");
                cfg.setPrefixUrl(Strings.CI.appendIfMissing(prefix, "/"));
                cfg.setHostnameVerifier(casSSLContext.getHostnameVerifier());
                cfg.setSslSocketFactory(casSSLContext.getSslContext().getSocketFactory());
                return new ConfigurableDelegatedClient(new CasClient(cfg), cas);
            })
            .collect(Collectors.toList());
    }
}
