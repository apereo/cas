package org.apereo.cas.configuration.model.core.authentication;

import lombok.extern.slf4j.Slf4j;
import org.apereo.cas.configuration.support.RequiresModule;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link GrouperPrincipalAttributesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-person-directory", automated = true)
@Slf4j
@Getter
@Setter
public class GrouperPrincipalAttributesProperties implements Serializable {

    private static final long serialVersionUID = 7139471665871712818L;

    /**
     * The order of this attribute repository in the chain of repositories.
     * Can be used to explicitly position this source in chain and affects
     * merging strategies.
     */
    private int order;

    /**
     * Enable the attribute repository source.
     */
    private boolean enabled;
}
