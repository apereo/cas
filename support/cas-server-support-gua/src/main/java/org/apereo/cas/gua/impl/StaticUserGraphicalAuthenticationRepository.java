package org.apereo.cas.gua.impl;

import com.google.common.io.ByteSource;
import org.apache.commons.io.IOUtils;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.configuration.model.support.gua.GraphicalUserAuthenticationProperties;
import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

import java.io.ByteArrayOutputStream;

/**
 * This is {@link StaticUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
public class StaticUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository {
    private static final long serialVersionUID = 421732017215881244L;
    private static final Logger LOGGER = LoggerFactory.getLogger(StaticUserGraphicalAuthenticationRepository.class);

    @Autowired
    private CasConfigurationProperties casProperties;

    @Autowired
    private ResourceLoader resourceLoader;

    @Override
    public ByteSource getGraphics(final String username) {
        try {
            final GraphicalUserAuthenticationProperties gua = casProperties.getAuthn().getGua();
            final Resource resource = resourceLoader.getResource(gua.getResource().getLocation());
            final ByteArrayOutputStream bos = new ByteArrayOutputStream();
            IOUtils.copy(resource.getInputStream(), bos);
            return ByteSource.wrap(bos.toByteArray());
        } catch (final Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return ByteSource.empty();
    }
}
