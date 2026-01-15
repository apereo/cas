package org.apereo.cas.configuration.model.support.email;

import module java.base;
import org.apereo.cas.configuration.features.CasFeatureModule;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 * This is {@link EmailProvidersProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-notifications", automated = true)
@Accessors(chain = true)
public class EmailProvidersProperties implements CasFeatureModule, Serializable {
    @Serial
    private static final long serialVersionUID = -7723886839517507396L;

    /**
     * Amazon SES settings.
     */
    @NestedConfigurationProperty
    private AmazonSesProperties ses = new AmazonSesProperties();

    /**
     * Mailjet settings.
     */
    @NestedConfigurationProperty
    private MailjetProperties mailjet = new MailjetProperties();

    /**
     * Mailgun settings.
     */
    @NestedConfigurationProperty
    private MailgunProperties mailgun = new MailgunProperties();

    /**
     * Microsoft settings.
     */
    @NestedConfigurationProperty
    private MicrosoftEmailProperties microsoft = new MicrosoftEmailProperties();
    
}
