package org.apereo.cas.configuration.model.core.web.tomcat;

import org.apereo.cas.configuration.support.RequiredProperty;
import org.apereo.cas.configuration.support.RequiresModule;

import com.fasterxml.jackson.annotation.JsonFilter;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.Serializable;

/**
 * This is {@link CasEmbeddedApacheTomcatRewriteValveProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.3.0
 */
@RequiresModule(name = "cas-server-webapp-tomcat")
@Getter
@Setter
@Accessors(chain = true)
@JsonFilter("CasEmbeddedApacheTomcatRewriteValveProperties")
public class CasEmbeddedApacheTomcatRewriteValveProperties implements Serializable {

    private static final long serialVersionUID = 9030094143985594411L;

    /**
     * Location of a rewrite valve specifically by Apache Tomcat
     * to activate URL rewriting.
     */
    @RequiredProperty
    private transient Resource location = new ClassPathResource("container/tomcat/rewrite.config");
}
