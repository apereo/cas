package org.apereo.cas.oidc.nativesso;

import org.apereo.cas.util.DigestUtils;
import org.apereo.cas.util.RandomUtils;

/**
 * This is {@link DefaultOidcDeviceSecretGenerator}.
 *
 * @author Misagh Moayyed
 * @since 7.3.0
 */
public class DefaultOidcDeviceSecretGenerator implements OidcDeviceSecretGenerator {

    private static final int DEVICE_SECRET_LENGTH = 64;

    @Override
    public String hash(final String deviceSecret) {
        return DigestUtils.sha512(deviceSecret);
    }

    @Override
    public String generate() {
        return "ODS-" + RandomUtils.randomAlphabetic(DEVICE_SECRET_LENGTH);
    }
}
