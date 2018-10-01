package org.apereo.cas.adaptors.swivel;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apereo.cas.authentication.AbstractMultifactorAuthenticationProvider;
import org.apereo.cas.configuration.model.support.mfa.SwivelMultifactorProperties;
import org.apereo.cas.services.RegisteredService;
import org.springframework.http.HttpMethod;
import java.net.HttpURLConnection;
import java.net.URL;
import lombok.NoArgsConstructor;

/**
 * This is {@link SwivelMultifactorAuthenticationProvider}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class SwivelMultifactorAuthenticationProvider extends AbstractMultifactorAuthenticationProvider {

    private static final long serialVersionUID = 498455080794156917L;

    private String swivelUrl;

    @Override
    public boolean isAvailable(final RegisteredService service) {
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
