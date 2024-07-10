package org.apereo.cas.configuration.model.support.email;

import org.apereo.cas.configuration.model.support.aws.BaseAmazonWebServicesProperties;
import org.apereo.cas.configuration.support.RequiresModule;
import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import java.io.Serial;

/**
 * This is {@link AmazonSesProperties}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-support-aws-ses", automated = true)
@Accessors(chain = true)
@JsonFilter("SimpleEmailProperties")
public class AmazonSesProperties extends BaseAmazonWebServicesProperties {
    @Serial
    private static final long serialVersionUID = -1202529110472766098L;

    private String sourceArn;

    private String configurationSetName;
}
