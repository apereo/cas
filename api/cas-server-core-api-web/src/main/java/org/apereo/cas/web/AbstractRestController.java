package org.apereo.cas.web;

import org.springframework.web.bind.annotation.RestController;

/**
 * This is {@link AbstractRestController}.
 * This base class holds annotation to prevent subclass beans from being picked up via bean scanning.
 * @author Hal Deadman
 * @since 7.3.0
 */
@RestController
public abstract class AbstractRestController {
}
