package org.apereo.cas.configuration.model.support.services.stream;

import org.apereo.cas.configuration.support.RequiresModule;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link BaseStreamServicesProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-service-registry-stream")
@Accessors(chain = true)
public class BaseStreamServicesProperties implements Serializable {

    private static final long serialVersionUID = 7025417314334269017L;
}
