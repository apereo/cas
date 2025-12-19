package org.apereo.cas.configuration.model.support.email;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.ExpressionLanguageCapable;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link MicrosoftEmailProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-mail-microsoft", automated = false)
@Accessors(chain = true)
public class MicrosoftEmailProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = -7723886839517507396L;

    /**
     * Client id used to authenticate to the service.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String clientId;
    /**
     * Client secret used to authenticate to the service.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String clientSecret;
    /**
     * Tenant id used to authenticate to the service.
     * The ultimate authority is built based on the tenant
     * id as {@code https://login.microsoftonline.com/{tenantId}}.
     */
    @RequiredProperty
    @ExpressionLanguageCapable
    private String tenantId;
    /**
     * Scope used to authenticate to the service.
     */
    private Set<String> scopes = Stream.of("https://outlook.office365.com/.default").collect(Collectors.toSet());
}
