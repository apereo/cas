package org.apereo.cas.configuration.model.support.pm;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link PasswordHistoryCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-pm-webflow")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("PasswordHistoryCoreProperties")
public class PasswordHistoryCoreProperties implements Serializable {
    private static final long serialVersionUID = 2212199066765183587L;

    /**
     * Flag to indicate if password history tracking is enabled.
     */
    private boolean enabled;
}
