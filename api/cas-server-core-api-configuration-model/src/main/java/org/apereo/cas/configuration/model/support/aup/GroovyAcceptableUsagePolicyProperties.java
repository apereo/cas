package org.apereo.cas.configuration.model.support.aup;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link GroovyAcceptableUsagePolicyProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.2.0
 */
@RequiresModule(name = "cas-server-support-aup-core")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("GroovyAcceptableUsagePolicyProperties")
public class GroovyAcceptableUsagePolicyProperties extends SpringResourceProperties {
    private static final long serialVersionUID = 9164227843747126083L;
}
