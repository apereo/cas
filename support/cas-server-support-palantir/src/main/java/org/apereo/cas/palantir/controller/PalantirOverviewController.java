package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.overview.PalantirOverview;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Answers the summary behind Palantir's home page in one request.
 *
 * @author Misagh Moayyed
 * @since 8.1.0
 */
@RequestMapping(PalantirConstants.URL_PATH_PALANTIR)
@RequiredArgsConstructor
@Tag(name = "Palantir")
public class PalantirOverviewController extends AbstractController {
    private final PalantirOverview overview;

    private final EndpointLinksResolver endpointLinksResolver;

    private final WebEndpointProperties webEndpointProperties;

    /**
     * The summary of the server for the home page. Administrators see every section whose endpoint is exposed;
     * other signed-in users see the server and its health only. The page can name the sections it shows, so the others
     * are not computed.
     *
     * @param sections the sections the page shows; all when not given
     * @param fresh    whether to compute the sections again rather than use the values kept from earlier
     * @return the summary
     */
    @GetMapping(path = "/dashboard/overview", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Summarize the server for the Palantir home page")
    public ResponseEntity<Map<String, Object>> overview(
        @RequestParam(name = "sections", required = false) @Nullable final Set<String> sections,
        @RequestParam(name = "fresh", required = false, defaultValue = "false") final boolean fresh) {
        val authentication = SecurityContextHolder.getContext().getAuthentication();
        val administrator = authentication != null && authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        val exposed = endpointLinksResolver.resolveLinks(webEndpointProperties.getBasePath()).keySet();
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(overview.summarize(exposed, administrator, sections == null ? Set.of() : sections, fresh));
    }
}
