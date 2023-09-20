package org.apereo.cas.palantir.controller;

import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.schema.SchemaGenerator;
import org.apereo.cas.services.BaseRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import lombok.val;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

/**
 * This is {@link SchemaController}.
 *
 * @author Misagh Moayyed
 * @since 7.0.0
 */
@RestController
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
public class SchemaController {
    
    /**
     * Generate JSON schema for services.
     *
     * @return the response entity
     */
    @GetMapping(path = "/schema/services",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity schema() {
        val results = SchemaGenerator.generate(BaseRegisteredService.class, List.of(RegexRegisteredService.class.getName()));
        return ResponseEntity.ok(results);
    }
}
