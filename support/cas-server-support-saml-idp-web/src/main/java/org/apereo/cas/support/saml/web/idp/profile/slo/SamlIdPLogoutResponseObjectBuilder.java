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
    private static final long serialVersionUID = 3841759791464862569L;

    public SamlIdPLogoutResponseObjectBuilder(final OpenSamlConfigBean configBean) {
        super(configBean);
    }
}
