package org.apereo.cas.support.oauth.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.support.oauth.authenticator.Authenticators;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.multihost.MultiHostUtils;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.pac4j.core.client.Client;
import org.pac4j.core.client.Clients;
import org.pac4j.core.client.finder.DefaultSecurityClientFinder;
import org.pac4j.core.context.WebContext;

import java.util.List;

/**
 * Custom {@link DefaultSecurityClientFinder} to rename the CAS OAuth client in case of multi-hosts.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@RequiredArgsConstructor
@Slf4j
public class MultiHostSecurityClientFinder extends DefaultSecurityClientFinder {

    private final CasConfigurationProperties casProperties;

    @Override
    public List<Client> find(final Clients clients, final WebContext context, final String clientNames) {
        var mhClientNames = clientNames;
        if (Authenticators.CAS_OAUTH_CLIENT.equals(mhClientNames)) {
            val currentPrefix = MultiHostUtils.computeServerPrefix(casProperties);
            mhClientNames = Authenticators.CAS_OAUTH_CLIENT + EncodingUtils.hexEncode(currentPrefix);
            LOGGER.debug("Translating requested clientNames to: [{}] for multi-hosts", mhClientNames);
        }
        return super.find(clients, context, mhClientNames);
    }
}
