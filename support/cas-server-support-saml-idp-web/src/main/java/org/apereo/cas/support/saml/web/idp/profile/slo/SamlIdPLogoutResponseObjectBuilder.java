package org.apereo.cas.support.saml.web.idp.profile.slo;

import org.apereo.cas.support.saml.OpenSamlConfigBean;
import org.apereo.cas.support.saml.util.AbstractSaml20ObjectBuilder;

/**
 * This is {@link SamlIdPLogoutResponseObjectBuilder}.
 *
 * @author Misagh Moayyed
 * @since 6.3.0
 */
public class SamlIdPLogoutResponseObjectBuilder extends AbstractSaml20ObjectBuilder {

    public SamlIdPLogoutResponseObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }
}
