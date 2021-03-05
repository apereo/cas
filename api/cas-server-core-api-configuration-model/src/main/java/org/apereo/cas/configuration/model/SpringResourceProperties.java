package org.apereo.cas.configuration.model;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link SpringResourceProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
@Getter
@Setter
@RequiresModule(name = "cas-server-core-util", automated = true)
@Accessors(chain = true)
@ToString
@JsonFilter("SpringResourceProperties")
public class SpringResourceProperties implements Serializable {
    private static final long serialVersionUID = 4142130961445546358L;
    /**
     * The location of the resource. Resources can be URLS, or
     * files found either on the classpath or outside somewhere
     * in the file system.
     */
    @RequiredProperty
    private transient Resource location;
}
