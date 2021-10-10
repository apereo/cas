package org.apereo.cas.configuration.model.support.consent;

import org.apereo.cas.configuration.model.core.util.EncryptionJwtSigningJwtCryptographyProperties;
import org.apereo.cas.configuration.model.core.web.flow.WebflowAutoConfigurationProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import org.apereo.cas.util.crypto.CipherExecutor;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.io.Serializable;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This is {@link ConsentCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-consent-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("ConsentCoreProperties")
public class ConsentCoreProperties implements Serializable {

    private static final long serialVersionUID = 5211308051524438384L;

    /**
     * Whether consent functionality should be enabled.
     */
    private boolean enabled = true;

    /**
     * Whether consent functionality should be globally
     * applicapable to all applications and requests.
     */
    private boolean active = true;

    /**
     * Global reminder time unit, to reconfirm consent
     * in cases no changes are detected.
     */
    private long reminder = 30;

    /**
     * Global reminder time unit of measure, to reconfirm consent
     * in cases no changes are detected.
     */
    private ChronoUnit reminderTimeUnit = ChronoUnit.DAYS;

    /**
     * Attributes that should always and globally be excluded
     * from the list of consentable attributes. Such attributes
     * are always ignored during consent rule calculations
     * and users will not be prmopted to consent to their release..
     */
    private List<String> excludedAttributes = Stream.of("eduPersonTargetedID").collect(Collectors.toList());

    /**
     * Signing/encryption settings.
     */
    @NestedConfigurationProperty
    private EncryptionJwtSigningJwtCryptographyProperties crypto = new EncryptionJwtSigningJwtCryptographyProperties();

    /**
     * The webflow consent configuration.
     */
    @NestedConfigurationProperty
    private WebflowAutoConfigurationProperties webflow = new WebflowAutoConfigurationProperties().setOrder(100);

    public ConsentCoreProperties() {
        crypto.getEncryption().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_ENCRYPTION_KEY_SIZE);
        crypto.getSigning().setKeySize(CipherExecutor.DEFAULT_STRINGABLE_SIGNING_KEY_SIZE);
    }
}
