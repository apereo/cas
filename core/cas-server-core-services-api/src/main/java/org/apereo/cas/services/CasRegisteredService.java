package org.apereo.cas.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.EqualsAndHashCode;

/**
 * This is {@link CasRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@EqualsAndHashCode(callSuper = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
public class CasRegisteredService extends AbstractRegisteredService {
    /**
     * The friendly name for this client.
     */
    public static final String FRIENDLY_NAME = "CAS Client";

    private static final long serialVersionUID = -2416680749378661897L;

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return FRIENDLY_NAME;
    }
}
