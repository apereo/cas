package org.apereo.cas.gua.impl;

import org.apereo.cas.gua.api.UserGraphicalAuthenticationRepository;

import com.google.common.io.ByteSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;

import java.io.ByteArrayOutputStream;

/**
 * This is {@link StaticUserGraphicalAuthenticationRepository}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@RequiredArgsConstructor
@Slf4j
public class StaticUserGraphicalAuthenticationRepository implements UserGraphicalAuthenticationRepository {
    private static final long serialVersionUID = 421732017215881244L;

    private final transient Resource graphicResource;

    @Override
    public ByteSource getGraphics(final String username) {
        try {
            val bos = new ByteArrayOutputStream();
            IOUtils.copy(this.graphicResource.getInputStream(), bos);
            return ByteSource.wrap(bos.toByteArray());
        } catch (final Exception e) {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(e.getMessage(), e);
            } else {
                LOGGER.error(e.getMessage());
            }
        }
        return ByteSource.empty();
    }
}
