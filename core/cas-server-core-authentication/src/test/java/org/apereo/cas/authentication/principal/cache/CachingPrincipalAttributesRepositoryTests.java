package org.apereo.cas.authentication.principal.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apereo.cas.authentication.principal.PrincipalAttributesRepository;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CachingPrincipalAttributesRepositoryTests extends AbstractCachingPrincipalAttributesRepositoryTests {

    private static final File JSON_FILE = new File("cachingPrincipalAttributesRepository.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final String unit, final long duration) {
        final CachingPrincipalAttributesRepository repo = new CachingPrincipalAttributesRepository(unit, duration);
        repo.setAttributeRepository(this.dao);
        return repo;
    }

    @Test
    public void verifySerializeACachingPrincipalAttributesRepositoryToJson() throws IOException {
        AbstractPrincipalAttributesRepository repositoryWritten = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.toString(), 1);

        mapper.writeValue(JSON_FILE, repositoryWritten);

        final PrincipalAttributesRepository repositoryRead = mapper.readValue(JSON_FILE, CachingPrincipalAttributesRepository.class);

        assertEquals(repositoryWritten, repositoryRead);
    }
}
