package org.apereo.cas.adaptors.yubikey;

import com.yubico.client.v2.ResponseStatus;
import com.yubico.client.v2.YubicoClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link DefaultYubiKeyAccountValidator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Slf4j
@RequiredArgsConstructor
public class DefaultYubiKeyAccountValidator implements YubiKeyAccountValidator {

    private final YubicoClient client;

    @Override
    public boolean isValid(final String uid, final String token) {
        try {
            val yubikeyPublicId = getTokenPublicId(token);
            if (StringUtils.isNotBlank(yubikeyPublicId)) {
                val response = this.client.verify(token);
                val status = response.getStatus();
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
