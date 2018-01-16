package org.apereo.cas.adaptors.azure;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.otp.authentication.OneTimeTokenCredential;

/**
 * This is {@link AzureAuthenticatorTokenCredential}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Slf4j
public class AzureAuthenticatorTokenCredential extends OneTimeTokenCredential {
    private static final long serialVersionUID = -7570600701132111037L;
}
