package org.apereo.cas.configuration.model.support.uma;

import module java.base;
import org.apereo.cas.configuration.support.DurationCapable;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link UmaPermissionTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-support-oauth-uma")
@Getter
@Setter
@Accessors(chain = true)
public class UmaPermissionTicketProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 6624128522839644377L;

    /**
     * Hard timeout to kill the UMA permission token and expire it.
     */
    @DurationCapable
    private String maxTimeToLiveInSeconds = "PT3M";

}
