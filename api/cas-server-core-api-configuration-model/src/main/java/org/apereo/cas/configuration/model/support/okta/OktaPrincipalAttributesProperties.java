package org.apereo.cas.configuration.model.support.okta;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link OktaPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-okta-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class OktaPrincipalAttributesProperties extends BaseOktaApiProperties {
    @Serial
    private static final long serialVersionUID = -6573755681498251678L;

    /**
     * Username attribute to fetch attributes by.
     */
    @RequiredProperty
    private String usernameAttribute = "username";

    /**
     * A value can be assigned to this field to uniquely identify this resolver.
     */
    private String id;
}
