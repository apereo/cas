package org.apereo.cas.configuration.model.support.uma;

import module java.base;
import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link UmaCoreProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Accessors(chain = true)
@Setter

public class UmaCoreProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 865028615694269276L;

    /**
     * UMA issuer.
     */
    @RequiredProperty
    private String issuer = "http://localhost:8080/cas";
}
