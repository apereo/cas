package org.apereo.cas.web;

import org.springframework.stereotype.Controller;

/**
 * This is {@link AbstractController}.
 * This base class holds annotation to prevent subclass beans from being picked up via bean scanning.
 *
 * @author Hal Deadman
 * @since 7.3.0
 */
@Controller
public abstract class AbstractController {
}
