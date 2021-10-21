package org.apereo.cas.configuration.model.support.saml.idp;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

/**
 * This is {@link AttributeQueryTicketProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-support-saml-idp")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("AttributeQueryTicketProperties")
public class AttributeQueryTicketProperties implements Serializable {

    private static final long serialVersionUID = -1690545027059561010L;

    /**
     * Number of seconds after which this ticket becomes invalid.
     */
    private long timeToKillInSeconds = TimeUnit.HOURS.toSeconds(8);
}
