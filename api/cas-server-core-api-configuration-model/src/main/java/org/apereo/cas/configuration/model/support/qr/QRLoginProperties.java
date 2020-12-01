package org.apereo.cas.configuration.model.support.qr;

import org.apereo.cas.configuration.model.SpringResourceProperties;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * This is {@link QRLoginProperties}.
 *
 * @author Ben Winston
 * @since 6.3.0
 */
@RequiresModule(name = "cas-server-support-qr")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("QRLoginProperties")
public class QRLoginProperties implements Serializable {
    private static final long serialVersionUID = 8726382874579042118L;

    /**
     * Default value for an instring
     */
    private String instring = "default_instring";

}
