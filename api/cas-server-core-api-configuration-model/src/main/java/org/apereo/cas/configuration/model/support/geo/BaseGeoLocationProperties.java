package org.apereo.cas.configuration.model.support.geo;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serial;
import java.io.Serializable;

/**
 * This is {@link BaseGeoLocationProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.0.0
 */
@Getter
@Setter
@Accessors(chain = true)
@RequiresModule(name = "cas-server-support-geolocation")
public abstract class BaseGeoLocationProperties implements Serializable {
    @Serial
    private static final long serialVersionUID = 4548572400079087989L;
}
