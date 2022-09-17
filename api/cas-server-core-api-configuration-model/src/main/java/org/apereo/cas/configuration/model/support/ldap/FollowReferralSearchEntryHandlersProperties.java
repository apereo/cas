package org.apereo.cas.configuration.model.support.ldap;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link FollowReferralSearchEntryHandlersProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@RequiresModule(name = "cas-server-support-ldap")
@Getter
@Accessors(chain = true)
@Setter
@JsonFilter("FollowReferralSearchEntryHandlersProperties")
public class FollowReferralSearchEntryHandlersProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 7138108925310792763L;

    /**
     * The default referral limit.
     */
    private int limit = 10;
}
