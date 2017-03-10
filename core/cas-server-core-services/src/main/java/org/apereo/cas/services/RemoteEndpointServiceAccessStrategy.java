package org.apereo.cas.services;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.http.HttpMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.net.URL;
import java.util.Map;

/**
 * This is {@link RemoteEndpointServiceAccessStrategy} that reaches out
 * to a remote endpoint, passing the CAS principal id to determine if access is allowed.
 * If the status code returned in the final response is not accepted by the policy here,
 * access shall be denied.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class RemoteEndpointServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -1108201604115278440L;

    private static final Logger LOGGER = LoggerFactory.getLogger(RemoteEndpointServiceAccessStrategy.class);

    private String endpointUrl;

    private String acceptableResponseCodes;

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal,
                                                           final Map<String, Object> principalAttributes) {
        try {
            if (super.doPrincipalAttributesAllowServiceAccess(principal, principalAttributes)) {
                final HttpClient client = ApplicationContextProvider.getApplicationContext()
                        .getBean("noRedirectHttpClient", HttpClient.class);

                final URIBuilder builder = new URIBuilder(this.endpointUrl);
                builder.addParameter("username", principal);
                final URL url = builder.build().toURL();
                final HttpMessage message = client.sendMessageToEndPoint(url);
                LOGGER.debug("Message received from [{}] is [{}]", url, message);
                return message != null && StringUtils.commaDelimitedListToSet(this.acceptableResponseCodes)
                        .contains(String.valueOf(message.getResponseCode()));
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }

        return false;
    }

    public String getEndpointUrl() {
        return endpointUrl;
    }

    public void setEndpointUrl(final String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    public String getAcceptableResponseCodes() {
        return acceptableResponseCodes;
    }

    public void setAcceptableResponseCodes(final String acceptableResponseCodes) {
        this.acceptableResponseCodes = acceptableResponseCodes;
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        final RemoteEndpointServiceAccessStrategy rhs = (RemoteEndpointServiceAccessStrategy) obj;
        return new EqualsBuilder()
                .appendSuper(super.equals(obj))
                .append(this.endpointUrl, rhs.endpointUrl)
                .append(this.acceptableResponseCodes, rhs.acceptableResponseCodes)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(endpointUrl)
                .append(acceptableResponseCodes)
                .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("endpointUrl", endpointUrl)
                .append("acceptableResponseCodes", acceptableResponseCodes)
                .toString();
    }
}
