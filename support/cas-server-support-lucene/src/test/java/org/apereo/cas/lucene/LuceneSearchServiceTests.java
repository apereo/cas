package org.apereo.cas.lucene;

import lombok.val;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import java.io.File;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/**
 * This is {@link LuceneSearchServiceTests}.
 *
 * @author Misagh Moayyed
 * @since 7.2.0
 */
@Tag("Simple")
class LuceneSearchServiceTests {
    private LuceneSearchService luceneSearchService;

    @BeforeEach
    void initialize() throws Exception {
        this.luceneSearchService = new LuceneSearchService(
            new File(FileUtils.getTempDirectory(), UUID.randomUUID().toString()),
            List.of("records", "metadata"),
            List.of("id", "name", "description"));
        luceneSearchService.deleteIndexes();
        try (val in = new ClassPathResource("records.json").getInputStream()) {
            luceneSearchService.createIndexes(in);
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"server.port", "server", "port", "server.", ".port"})
    void verifyByName(final String query) throws Exception {
        var result = luceneSearchService.first("name", query);
        assertEquals(1, result.getLong("id").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"fancy-name", "fancy-", "-name"})
    void verifyTokensInName(final String query) throws Exception {
        var result = luceneSearchService.first("name", query);
        assertEquals(3, result.getLong("id").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {"new field", "with-a"})
    void verifyDescription(final String query) throws Exception {
        var result = luceneSearchService.first("description", query);
        assertEquals(2, result.getLong("id").orElseThrow());
    }
}
