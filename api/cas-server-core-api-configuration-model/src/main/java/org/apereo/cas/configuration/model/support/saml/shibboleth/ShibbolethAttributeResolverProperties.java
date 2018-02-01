package org.apereo.cas.configuration.model.support.saml.shibboleth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Getter;
import lombok.Setter;

/**
 * This is {@link ShibbolethAttributeResolverProperties}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
@Slf4j
@Getter
@Setter
public class ShibbolethAttributeResolverProperties implements Serializable {

    private static final long serialVersionUID = 6315982182145371022L;

    /**
     * List of Shibboleth's attribute resolver XMLM resources to parse and load.
     * Each resource can either be found on the file system, as a classpath entry
     * or via a URL if needed.
     */
    private List<Resource> resources = Stream.of(new ClassPathResource("attribute-resolver.xml")).collect(Collectors.toList());
}
