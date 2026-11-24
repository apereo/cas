package org.apereo.cas.palantir.controller;

import module java.base;
import org.apereo.cas.palantir.PalantirAuthorities;
import org.apereo.cas.palantir.PalantirConstants;
import org.apereo.cas.palantir.overview.PalantirOverview;
import org.apereo.cas.web.AbstractController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.actuate.autoconfigure.endpoint.web.WebEndpointProperties;
import org.springframework.boot.actuate.endpoint.SecurityContext;
import org.springframework.boot.actuate.endpoint.web.EndpointLinksResolver;
import org.springframework.boot.health.autoconfigure.actuate.endpoint.HealthEndpointProperties;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
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

    private final ObjectProvider<HealthEndpointProperties> healthEndpointProperties;

    /**
     * The summary of the server for the home page. Administrators see every section whose endpoint is exposed;
     * other signed-in users see the server and its health only, and the names of the health checks only when the
     * health endpoint shows its components to them. The page can name the sections it shows, so the others
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
        val administrator = PalantirAuthorities.isAdministrator(authentication);
        val exposed = endpointLinksResolver.resolveLinks(webEndpointProperties.getBasePath()).keySet();
        val summary = overview.summarize(exposed, administrator, sections == null ? Set.of() : sections, fresh);
        if (!administrator && !healthComponentsShown(authentication)) {
            hideHealthComponents(summary);
        }
        return ResponseEntity.ok()
            .cacheControl(CacheControl.noStore())
            .body(summary);
    }

    /**
     * Whether the health endpoint shows its components to this person, as it is configured to: its components setting,
     * or its details setting when components are not set, and the roles it asks for. Without the health endpoint's
     * settings, components are not shown.
     *
     * @param authentication who is asking
     * @return true when the health checks may be named to them
     */
    private boolean healthComponentsShown(final @Nullable Authentication authentication) {
        val properties = healthEndpointProperties.getIfAvailable();
        if (properties == null) {
            return false;
        }
        val show = Objects.requireNonNullElseGet(properties.getShowComponents(), properties::getShowDetails);
        return show.isShown(new AuthenticationSecurityContext(authentication), properties.getRoles());
    }

    private static void hideHealthComponents(final Map<String, Object> summary) {
        if (summary.get("sections") instanceof final Map<?, ?> sections && sections.get("health") instanceof final Map<?, ?> health) {
            val kept = new LinkedHashMap<Object, Object>(health);
            kept.remove("components");
            val writable = (Map<Object, Object>) sections;
            writable.put("health", kept);
        }
    }

    private record AuthenticationSecurityContext(@Nullable Authentication authentication) implements SecurityContext {
        @Override
        public @Nullable Principal getPrincipal() {
            return authentication != null && authentication.isAuthenticated() ? authentication : null;
        }

        @Override
        public boolean isUserInRole(final String role) {
            return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .filter(Objects::nonNull)
                .anyMatch(authority -> authority.equals(role) || authority.equals("ROLE_" + role));
        }
    }
}
