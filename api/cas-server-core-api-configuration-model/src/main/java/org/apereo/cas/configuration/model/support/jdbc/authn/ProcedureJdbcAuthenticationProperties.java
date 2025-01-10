package org.apereo.cas.configuration.model.support.jdbc.authn;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link ProcedureJdbcAuthenticationProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@RequiresModule(name = "cas-server-support-jdbc-authentication")
@Getter
@Setter
@Accessors(chain = true)
public class ProcedureJdbcAuthenticationProperties extends BaseJdbcAuthenticationProperties {
    @Serial
    private static final long serialVersionUID = 4268982716707687796L;

    /**
     * The procedure name to execute.
     */
    @RequiredProperty
    private String procedureName;
}
