package org.apereo.cas.adaptors.azure;

import net.phonefactor.pfsdk.PFAuthParams;
import net.phonefactor.pfsdk.PlainTextPinInfo;
import net.phonefactor.pfsdk.StandardPinInfo;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.configuration.model.support.mfa.AzureMultifactorProperties;

/**
 * This is {@link AzureAuthenticatorAuthenticationRequestBuilder}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class AzureAuthenticatorAuthenticationRequestBuilder {
    private final String phoneAttributeName;
    private final AzureMultifactorProperties.AuthenticationModes mode;

    public AzureAuthenticatorAuthenticationRequestBuilder(final String phoneAttributeName,
                                                          final AzureMultifactorProperties.AuthenticationModes mode) {
        this.phoneAttributeName = phoneAttributeName;
        this.mode = mode;
    }

    /**
     * Build pf auth params.
     *
     * @param p the principal
     * @param c the credential/token
     * @return the pf auth params
     */
    public PFAuthParams build(final Principal p, final AzureAuthenticatorTokenCredential c) {
        if (!p.getAttributes().containsKey(this.phoneAttributeName)) {
            throw new IllegalArgumentException(this.phoneAttributeName + " is not available as a principal attribute");
        }
        
        final PFAuthParams params = new PFAuthParams();
        params.setPhoneNumber(p.getAttributes().get(this.phoneAttributeName).toString());
        params.setCountryCode("1");
        params.setUsername(p.getId());

        switch (mode) {
            case PIN:
                params.setAuthInfo(new PlainTextPinInfo(c.getToken()));
                break;
            case POUND:
            default:
                params.setAuthInfo(new StandardPinInfo());
        }
        return params;
    }
}
