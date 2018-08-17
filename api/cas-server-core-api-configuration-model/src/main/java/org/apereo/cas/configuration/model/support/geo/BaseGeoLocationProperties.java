package org.apereo.cas.configuration.model.support.geo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * This is {@link BaseGeoLocationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
public abstract class BaseGeoLocationProperties implements Serializable {
    private static final long serialVersionUID = 4548572400079087989L;

    /**
     * The access key for ip-stack used to look up ip addresses
     * for geo locations. See <a href="http://ipstack.com">this link</a> for more info.
     */
    private String ipStackApiAccessKey;
}
