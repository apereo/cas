package org.apereo.cas.configuration.model.core.web.view;

import module java.base;
import org.apereo.cas.configuration.support.RequiresModule;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * This is {@link Cas20ProxyViewProperties}.
 *
 * @author Misagh Moayyed
 * @since 6.4.0
 */
@RequiresModule(name = "cas-server-core-web", automated = true)
@Getter
@Setter
@Accessors(chain = true)
public class Cas20ProxyViewProperties implements Serializable {

    @Serial
    private static final long serialVersionUID = 6765987342872282599L;

    /**
     * The relative location of the CAS2 proxy success view bean.
     */
    private String success = "protocol/2.0/casProxySuccessView";

    /**
     * The relative location of the CAS2 proxy failure view bean.
     */
    private String failure = "protocol/2.0/casProxyFailureView";
}
