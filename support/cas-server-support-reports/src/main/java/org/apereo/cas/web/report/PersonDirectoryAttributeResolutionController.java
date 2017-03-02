package org.apereo.cas.web.report;

import org.apereo.cas.authentication.BasicIdentifiableCredential;
import org.apereo.cas.authentication.principal.Principal;
import org.apereo.cas.authentication.principal.PrincipalResolver;
import org.apereo.cas.configuration.CasConfigurationProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This is {@link PersonDirectoryAttributeResolutionController}.
 *
 * @author Misagh Moayyed
 * @since 5.0.0
 */
public class PersonDirectoryAttributeResolutionController extends BaseCasMvcEndpoint {

    @Autowired
    @Qualifier("personDirectoryPrincipalResolver")
    private PrincipalResolver personDirectoryPrincipalResolver;

    public PersonDirectoryAttributeResolutionController(final CasConfigurationProperties casProperties) {
        super("attrresolution", "/attrresolution", casProperties.getMonitor().getEndpoints().getAttributeResolution(), casProperties);
    }

    /**
     * Handle request.
     *
     * @param request  the request
     * @param response the response
     * @return the model and view
     * @throws Exception the exception
     */
    @GetMapping
    protected ModelAndView handleRequestInternal(final HttpServletRequest request,
                                                 final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);
        return new ModelAndView("monitoring/attrresolution");
    }

    /**
     * Resolve principal attributes map.
     *
     * @param uid      the uid
     * @param request  the request
     * @param response the response
     * @return the map
     * @throws Exception the exception
     */
    @PostMapping(value = "/resolveattrs")
    @ResponseBody
    public Map<String, Object> resolvePrincipalAttributes(@RequestParam final String uid,
                                                          final HttpServletRequest request,
                                                          final HttpServletResponse response) throws Exception {
        ensureEndpointAccessIsAuthorized(request, response);
        final Principal p = personDirectoryPrincipalResolver.resolve(new BasicIdentifiableCredential(uid));
        final Map<String, Object> map = new LinkedHashMap<>();
        map.put("uid", p.getId());
        map.put("attributes", p.getAttributes());
        return map;
    }
}
