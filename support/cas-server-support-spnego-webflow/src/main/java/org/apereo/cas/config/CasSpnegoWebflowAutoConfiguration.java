package org.apereo.cas.config;

import module java.base;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

/**
 * This is {@link CasSpnegoWebflowAutoConfiguration}.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@AutoConfiguration
@Import({
    SpnegoWebflowConfiguration.class,
    SpnegoWebflowActionsConfiguration.class
})
public class CasSpnegoWebflowAutoConfiguration {
}
