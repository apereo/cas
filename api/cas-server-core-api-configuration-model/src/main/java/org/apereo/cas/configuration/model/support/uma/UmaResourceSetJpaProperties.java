package org.apereo.cas.configuration.model.support.uma;

import org.apereo.cas.configuration.model.support.jpa.AbstractJpaProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

/**
 * This is {@link UmaResourceSetJpaProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("UmaResourceSetJpaProperties")
public class UmaResourceSetJpaProperties extends AbstractJpaProperties {
    private static final long serialVersionUID = 210435146313504995L;

    public UmaResourceSetJpaProperties() {
        super.setUrl(StringUtils.EMPTY);
    }
}
