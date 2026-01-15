package org.apereo.cas.authentication.device;

import module java.base;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

/**
 * This is {@link MultifactorAuthenticationRegisteredDevice}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@SuperBuilder
@Getter
@ToString(of = {"name", "type", "id", "model"})
@EqualsAndHashCode(of = {"name", "type", "id", "model", "source"})
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@Setter
@NoArgsConstructor(force = true)
@With
@AllArgsConstructor
public class MultifactorAuthenticationRegisteredDevice implements Serializable {
    @Serial
    private static final long serialVersionUID = 1040948239519297651L;

    /**
     * The device name as chosen/managed by the mfa provider.
     */
    private final String name;

    /**
     * Phone, tablet, etc.
     */
    private final String type;

    /**
     * Unique identifier for the device registration entry.
     */
    private final String id;

    /**
     * Phone number assigned to the device, if any.
     */
    private final String number;

    /**
     * Platform type and details. (Android, iOS, Samsung, LG, etc)
     */
    private final String model;

    private final String lastUsedDateTime;

    private final String expirationDateTime;

    /**
     * The actual device record produced by the provider
     * typically captured here as JSON.
     */
    private final String payload;

    private final String source;

    @Builder.Default
    private Map<String, Object> details = new LinkedHashMap<>();
}
