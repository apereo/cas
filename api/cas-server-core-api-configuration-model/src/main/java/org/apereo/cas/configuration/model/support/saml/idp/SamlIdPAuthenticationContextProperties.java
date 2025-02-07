package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This is {@link SamlIdPAuthenticationContextProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)

public class SamlIdPAuthenticationContextProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = -2848175783676789852L;

    /**
     * A mapping of authentication context class refs.
     * This is where specific authentication context classes
     * are referenced and mapped to providers that CAS may support
     * mainly for, i.e. MFA purposes.
     * <p>
     * Example might be {@code urn:oasis:names:tc:SAML:2.0:ac:classes:SomeClassName->mfa-duo}.
     * <p>
     * In delegated authentication scenarios, this can also be a mapping of authentication context class refs,
     * when CAS is proxying/delegating authentication to an external SAML2 identity provider. The requested authentication context
     * as submitted by the service provider is first received by CAS, and then gets mapped to
     * a context class that is passed onto the external identity provider. For example, you might have a scenario
     * where a SAML2 service provider would submit {@code https://refeds.org/profile/mfa} to CAS, and CAS would
     * translate that to {@code http://schemas.microsoft.com/claims/multipleauthn} to ultimate route the
     * authentication request to Azure. If no mapping is found, the original context is passed as is.
     * <p>
     * Example might be {@code https://refeds.org/profile/mfa->http://schemas.microsoft.com/claims/multipleauthn}.
     */
    private List<String> authenticationContextClassMappings = new ArrayList<>();

    /**
     * The default authentication context class to include in the response
     * if none is specified via the service.
     */
    private String defaultAuthenticationContextClass = "urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport";

}
