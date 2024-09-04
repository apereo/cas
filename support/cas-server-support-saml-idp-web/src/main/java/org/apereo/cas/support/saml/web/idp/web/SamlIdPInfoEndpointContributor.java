package org.apereo.cas.support.saml.web.idp.web;

import org.apereo.cas.configuration.CasConfigurationProperties;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import java.util.Map;

/**
 * This is {@link SamlIdPInfoEndpointContributor}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RequiredArgsConstructor
public class SamlIdPInfoEndpointContributor implements InfoContributor {
    private final CasConfigurationProperties casProperties;
    
    @Override
    public void contribute(final Info.Builder builder) {
        val details = Map.of("entityId", casProperties.getAuthn().getSamlIdp().getCore().getEntityId());
        builder.withDetails(Map.of("saml2", details));
    }
}
