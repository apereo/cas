package org.apereo.cas.adaptors.yubikey;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.rest.RestHttpRequestCredentialFactory;
import org.apereo.cas.util.CollectionUtils;
import org.springframework.util.MultiValueMap;

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


    @Override
    public List<Credential> fromRequestBody(final MultiValueMap<String, String> requestBody) {
        final String otp = requestBody.getFirst("yubikeyotp");
        LOGGER.debug("YubiKey token in the request body: [{}]", otp);
        if (StringUtils.isBlank(otp)) {
            return new ArrayList<>(0);
        }
        return CollectionUtils.wrap(new YubiKeyCredential(otp));
    }
}
