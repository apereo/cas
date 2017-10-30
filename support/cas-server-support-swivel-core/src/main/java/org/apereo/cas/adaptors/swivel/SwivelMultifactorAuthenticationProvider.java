package org.apereo.cas.adaptors.swivel;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * This is {@link SwivelMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class SwivelMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {
    private static final long serialVersionUID = 498455080794156917L;
    private static final Logger LOGGER = LoggerFactory.getLogger(SwivelMultifactorAuthenticationProvider.class);

    private String swivelUrl;

    /**
     * Required for serialization and reflection.
     */
    public SwivelMultifactorAuthenticationProvider() {
    }
    
    
    public SwivelMultifactorAuthenticationProvider(final String swivelUrl) {
        this.swivelUrl = swivelUrl;
    }

    @Override
    protected boolean isAvailable() {
        return canPing();
    }

    @Override
    public String getFriendlyName() {
        return "Swivel Secure";
    }

    @Override
    public String getId() {
        return StringUtils.defaultIfBlank(super.getId(), SwivelMultifactorProperties.DEFAULT_IDENTIFIER);
    }

    /**
     * Can ping provider?
     *
     * @return the boolean
     */
    public boolean canPing() {
        try {
            final HttpURLConnection connection = (HttpURLConnection) new URL(this.swivelUrl).openConnection();
            connection.setRequestMethod(HttpMethod.GET.name());
            connection.connect();
            return connection.getResponseCode() == HttpStatus.SC_OK;
        } catch (final Exception e) {
            LOGGER.warn(e.getMessage(), e);
        }
        return false;
    }
}
