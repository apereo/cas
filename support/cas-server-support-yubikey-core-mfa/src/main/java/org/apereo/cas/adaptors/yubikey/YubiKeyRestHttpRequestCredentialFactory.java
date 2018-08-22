package org.apereo.cas.adaptors.yubikey;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.factory.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

import javax.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link YubiKeyRestHttpRequestCredentialFactory}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Slf4j
public class YubiKeyRestHttpRequestCredentialFactory implements RestHttpRequestCredentialFactory {
    /**
     * OTP token found in the request body.
     */
    public static final String PARAMETER_NAME_YUBIKEY_OTP = "yubikeyotp";

    @Override
    public List<Credential> fromRequest(final HttpServletRequest request, final MultiValueMap<String, String> requestBody) {
        if (requestBody == null || requestBody.isEmpty()) {
            LOGGER.debug("Skipping {} because the requestBody is null or empty", getClass().getSimpleName());
            return new ArrayList<>(0);
        }
        final String otp = requestBody.getFirst(PARAMETER_NAME_YUBIKEY_OTP);
        LOGGER.debug("YubiKey token in the request body: [{}]", otp);
        if (StringUtils.isBlank(otp)) {
            return new ArrayList<>(0);
        }
        return CollectionUtils.wrap(new YubiKeyCredential(otp));
    }
}
