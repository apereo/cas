package org.apereo.cas.configuration.model.core.web.view;

import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

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
@JsonFilter("Cas20ProxyViewProperties")
public class Cas20ProxyViewProperties implements Serializable {

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
