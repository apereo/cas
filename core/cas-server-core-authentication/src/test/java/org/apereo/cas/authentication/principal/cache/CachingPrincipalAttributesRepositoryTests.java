package org.apereo.cas.authentication.principal.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Handles tests for {@link CachingPrincipalAttributesRepository}.
 *
 * @author Misagh Moayyed
 * @since 4.1
 */
public class CachingPrincipalAttributesRepositoryTests extends AbstractCachingPrincipalAttributesRepositoryTests {

    private static final File JSON_FILE = new File(FileUtils.getTempDirectoryPath(), "cachingPrincipalAttributesRepository.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    @Override
    protected AbstractPrincipalAttributesRepository getPrincipalAttributesRepository(final String unit, final long duration) {
        val repo = new CachingPrincipalAttributesRepository(unit, duration);
        repo.setAttributeRepository(this.dao);
        return repo;
    }

    @Test
    public void verifySerializeACachingPrincipalAttributesRepositoryToJson() throws IOException {
        val repositoryWritten = getPrincipalAttributesRepository(TimeUnit.MILLISECONDS.toString(), 1);
        MAPPER.writeValue(JSON_FILE, repositoryWritten);
        val repositoryRead = MAPPER.readValue(JSON_FILE, CachingPrincipalAttributesRepository.class);
        assertEquals(repositoryWritten, repositoryRead);
    }
}
