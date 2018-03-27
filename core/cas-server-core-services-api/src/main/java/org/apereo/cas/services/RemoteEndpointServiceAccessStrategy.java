package org.apereo.cas.services;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.URIBuilder;
import org.apereo.cas.util.http.HttpClient;
import org.apereo.cas.util.spring.ApplicationContextProvider;
import org.apereo.cas.util.http.HttpMessage;
import org.springframework.util.StringUtils;
import java.net.URL;
import java.util.Map;
import lombok.Getter;

/**
 * This is {@link RemoteEndpointServiceAccessStrategy} that reaches out
 * to a remote endpoint, passing the CAS principal id to determine if access is allowed.
 * If the status code returned in the final response is not accepted by the policy here,
 * access shall be denied.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@ToString(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RemoteEndpointServiceAccessStrategy extends DefaultRegisteredServiceAccessStrategy {

    private static final long serialVersionUID = -1108201604115278440L;

    private String endpointUrl;

    private String acceptableResponseCodes;

    @Override
    public boolean doPrincipalAttributesAllowServiceAccess(final String principal, final Map<String, Object> principalAttributes) {
        try {
            if (super.doPrincipalAttributesAllowServiceAccess(principal, principalAttributes)) {
                final HttpClient client = ApplicationContextProvider.getApplicationContext().getBean("noRedirectHttpClient", HttpClient.class);
                final URIBuilder builder = new URIBuilder(this.endpointUrl);
                builder.addParameter("username", principal);
                final URL url = builder.build().toURL();
                final HttpMessage message = client.sendMessageToEndPoint(url);
                LOGGER.debug("Message received from [{}] is [{}]", url, message);
                return message != null && StringUtils.commaDelimitedListToSet(this.acceptableResponseCodes).contains(String.valueOf(message.getResponseCode()));
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }

}
