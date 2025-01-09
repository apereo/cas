package org.apereo.cas.multihost;

import org.apereo.cas.configuration.model.core.multihost.SimpleHostProperties;

import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfo;
import org.apereo.inspektr.common.web.ClientInfoExtractionOptions;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.apereo.inspektr.common.web.ClientInfoThreadLocalFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.List;

/**
 * The {@link ClientInfoThreadLocalFilter} supplemented with the current host.
 *
 * @author Jerome LELEU
 * @since 7.2.0
 */
@Slf4j
public class MultiHostClientInfoThreadLocalFilter extends ClientInfoThreadLocalFilter {
    private final ClientInfoExtractionOptions options;
    private final SimpleHostProperties primaryHost;
    private final List<SimpleHostProperties> secondariesHosts;

    public MultiHostClientInfoThreadLocalFilter(final ClientInfoExtractionOptions options,
                                                final SimpleHostProperties primaryHost,
                                                final List<SimpleHostProperties> secondariesHosts) {
        super(options);
        this.options = options;
        this.primaryHost = primaryHost;
        this.secondariesHosts = secondariesHosts;
    }

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
                         final FilterChain filterChain) throws IOException, ServletException {
        try {
            if (request instanceof final HttpServletRequest httpServletRequest) {
                val clientInfo = ClientInfo.from(httpServletRequest, options);

                var currentHost = primaryHost;
                val url = request != null ? httpServletRequest.getRequestURL().toString() : null;
                LOGGER.trace("Request URL: [{}]", url);
                if (StringUtils.isNotBlank(url)) {
                    for (val secondaryHost : secondariesHosts) {
                        if (url.startsWith(secondaryHost.getServerPrefix())) {
                            currentHost = secondaryHost;
                            break;
                        }
                    }
                }
                LOGGER.trace("Current host: [{}]", currentHost);

                ClientInfoHolder.setClientInfo(new MultiHostClientInfo(clientInfo, currentHost));
            }
            filterChain.doFilter(request, response);
        } finally {
            ClientInfoHolder.clear();
        }
    }
}
