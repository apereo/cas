package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.VerificationResponse;
import com.yubico.client.v2.YubicoClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is {@link DefaultYubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class DefaultYubiKeyAccountValidator implements YubiKeyAccountValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultYubiKeyAccountValidator.class);
    private final YubicoClient client;

    public DefaultYubiKeyAccountValidator(final YubicoClient client) {
        this.client = client;
    }

    @Override
    public boolean isValid(final String uid, final String token) {
        try {
            final String yubikeyPublicId = YubicoClient.getPublicId(token);
            if (StringUtils.isNotBlank(yubikeyPublicId)) {
                final VerificationResponse response = this.client.verify(token);
                final ResponseStatus status = response.getStatus();
                if (status.compareTo(ResponseStatus.OK) == 0) {
                    LOGGER.debug("YubiKey response status [{}] at [{}]", status, response.getTimestamp());
                    return true;
                }
                LOGGER.error("Failed to verify YubiKey token: [{}]", response);
            } else {
                LOGGER.error("Invalid YubiKey token: [{}]", token);
            }
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return false;
    }
}
