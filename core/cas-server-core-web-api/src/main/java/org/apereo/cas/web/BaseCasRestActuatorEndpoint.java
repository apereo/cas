package org.apereo.cas.web;

import module java.base;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.apereo.cas.util.CompressionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import org.apereo.cas.util.spring.RestActuatorEndpoint;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import tools.jackson.databind.ObjectMapper;

/**
 * This is {@link BaseCasRestActuatorEndpoint}.
 * This class serves as the parent component for all actuator
 * endpoints that need to support REST-like operations via Spring MVC.
 * Such endpoints typically need direct access to the {@link jakarta.servlet.http.HttpServletRequest}.
 * This class exists to account for the removal and deprecation of {@code RestControllerEndpoint} annotation.
 *
 * @author Misagh Moayyed
 * @since 7.1.0
 */
@RestActuatorEndpoint
@Slf4j
public abstract class BaseCasRestActuatorEndpoint extends BaseCasActuatorEndpoint {
    protected static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .defaultTypingEnabled(true).build().toObjectMapper();

    /**
     * The size, in bytes, one record of an imported archive may have at most.
     */
    protected static final int MAXIMUM_IMPORTED_RECORD_SIZE = 1024 * 1024;

    /**
     * The size, in bytes, the records of an imported archive may have together at most, once decompressed.
     */
    protected static final long MAXIMUM_IMPORTED_ARCHIVE_SIZE = 64L * 1024 * 1024;

    /**
     * The media type of a zip archive of records, as an export writes it.
     */
    protected static final String ZIP_MEDIA_TYPE = "application/zip";

    /**
     * The media type some browsers give a zip archive.
     */
    protected static final String ZIP_COMPRESSED_MEDIA_TYPE = "application/x-zip-compressed";

    protected final ConfigurableApplicationContext applicationContext;

    protected BaseCasRestActuatorEndpoint(final CasConfigurationProperties casProperties,
                                          final ConfigurableApplicationContext applicationContext) {
        super(casProperties);
        this.applicationContext = applicationContext;
    }

    /**
     * Head response entity.
     *
     * @return the response entity
     */
    @Operation(summary = "Reports back to the presence of the endpoint without any content")
    @RequestMapping(method = RequestMethod.HEAD, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity head() {
        return ResponseEntity.ok().build();
    }

    /**
     * Import every record of a zip archive, as an export writes it, one record per file. An archive with a file, or
     * files together, larger than the import allows is refused as too large, and nothing of it is imported.
     *
     * @param input    the archive
     * @param importer stores one record and tells whether it was stored
     * @return how many records were stored and how many were not
     * @throws IOException when the archive cannot be read
     */
    protected static ResponseEntity<Map<String, Integer>> importArchive(final InputStream input,
                                                                        final Predicate<String> importer)
            throws IOException {
        return importArchive(input, name -> true, importer);
    }

    /**
     * Import the records of a zip archive whose files are accepted by name, one record per file, with the same limits
     * and answer as {@link #importArchive(InputStream, Predicate)}. A file whose name is not accepted is passed over,
     * and counts neither as imported nor as skipped.
     *
     * @param input    the archive
     * @param accepted whether a file, by its name in the archive, holds a record
     * @param importer stores one record and tells whether it was stored
     * @return how many records were stored and how many were not
     * @throws IOException when the archive cannot be read
     */
    protected static ResponseEntity<Map<String, Integer>> importArchive(final InputStream input,
                                                                        final Predicate<String> accepted,
                                                                        final Predicate<String> importer)
            throws IOException {
        try {
            val records = CompressionUtils.readZipEntries(input, MAXIMUM_IMPORTED_RECORD_SIZE, MAXIMUM_IMPORTED_ARCHIVE_SIZE, accepted);
            val imported = (int) records.stream().filter(importer).count();
            return importedRecords(imported, records.size() - imported);
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("Unable to import the archive: [{}]", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE).build();
        }
    }

    /**
     * The answer to an imported archive: how many of its records were stored and how many were not, created when at
     * least one was stored and unprocessable when none was.
     *
     * @param imported how many records were stored
     * @param skipped  how many records were not
     * @return the answer
     */
    protected static ResponseEntity<Map<String, Integer>> importedRecords(final int imported, final int skipped) {
        return ResponseEntity.status(imported > 0 || skipped == 0 ? HttpStatus.CREATED : HttpStatus.UNPROCESSABLE_CONTENT)
            .body(Map.of("imported", imported, "skipped", skipped));
    }
}
