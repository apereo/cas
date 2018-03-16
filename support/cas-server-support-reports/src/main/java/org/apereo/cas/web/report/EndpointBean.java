package org.apereo.cas.web.report;

import lombok.Data;

import java.io.Serializable;

/**
 * This is {@link EndpointBean}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@Data
public class EndpointBean implements Serializable {
    private static final long serialVersionUID = -3446962071459197099L;
    private String name;
    private String title;
}
