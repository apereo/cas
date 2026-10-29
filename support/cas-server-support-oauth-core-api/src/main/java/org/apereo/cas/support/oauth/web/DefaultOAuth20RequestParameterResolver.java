package org.apereo.cas.support.oauth.web;

import module java.base;
import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.services.RegisteredServiceOAuthResourceMatcher;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.http.HttpRequestUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;

/**
 * This is {@link DefaultOAuth20RequestParameterResolver}.
 *
 * @author Misagh Moayyed
 * @since 6.6.0
 */
@RequiredArgsConstructor
@Slf4j
public class DefaultOAuth20RequestParameterResolver implements OAuth20RequestParameterResolver {
    private static final ObjectMapper MAPPER = JacksonObjectMapperFactory.builder()
        .singleArrayElementUnwrapped(true).build().toObjectMapper();

    protected final JwtBuilder jwtBuilder;

    @Override
    public OAuth20ResponseTypes resolveResponseType(final HttpServletRequest request) {
        val responseTypesSupport = getResponseTypesSupported(request);
        val responseType = resolveRequestParameter(request, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf)
            .filter(typeName -> responseTypesSupport.stream().anyMatch(supported -> "*".equalsIgnoreCase(supported) || supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseTypes.CODE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }
    
    @Override
    public OAuth20GrantTypes resolveGrantType(final HttpServletRequest request) {
        val grantTypesSupport = getGrantTypesSupported(request);
        val grantType = resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf)
            .filter(typeName -> grantTypesSupport.stream().anyMatch(supported -> "*".equalsIgnoreCase(supported) || supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20GrantTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(grantType))
            .findFirst()
            .orElse(OAuth20GrantTypes.NONE);
        LOGGER.debug("OAuth grant type is [{}]", type);
        return type;
    }

    @Override
    public OAuth20ResponseModeTypes resolveResponseModeType(final HttpServletRequest request) {
        val supportedResponseModes = getResponseModesSupported(request);
        val responseType = resolveRequestParameter(request, OAuth20Constants.RESPONSE_MODE)
            .map(String::valueOf)
            .filter(typeName -> supportedResponseModes.stream().anyMatch(supported -> "*".equalsIgnoreCase(supported) || supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseModeTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseModeTypes.NONE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }
    
    @Override
    public <T> @Nullable T resolveJwtRequestParameter(final String jwtRequest, final RegisteredService registeredService,
                                                      final String name, final Class<T> clazz)
            throws Exception {
        val jwt = jwtBuilder.unpack(Optional.ofNullable(registeredService), jwtRequest);
        return jwt == null ? null : getJwtRequestClaim(jwt, name, clazz);
    }

    /*
     * The request object is unpacked and verified once per HTTP request; every parameter read after that
     * comes from the same claims.
     */
    @Override
    public <T> @Nullable T resolveJwtRequestParameter(final HttpServletRequest request, final String jwtRequest,
                                            final String name, final Class<T> clazz) {
        val jwt = unpackJwtRequest(request, jwtRequest);
        return jwt == null ? null : FunctionUtils.doUnchecked(() -> getJwtRequestClaim(jwt, name, clazz));
    }

    @Override
    public <T> Map<String, T> resolveJwtRequestParameters(final String jwtRequest, final RegisteredService registeredService,
                                                          final Collection<String> names, final Class<T> clazz)
            throws Exception {
        val jwt = jwtBuilder.unpack(Optional.ofNullable(registeredService), jwtRequest);
        val parameters = new LinkedHashMap<String, T>();
        if (jwt != null) {
            for (val name : names) {
                val value = getJwtRequestClaim(jwt, name, clazz);
                if (value != null) {
                    parameters.put(name, value);
                }
            }
        }
        return parameters;
    }

    private @Nullable JWTClaimsSet unpackJwtRequest(final HttpServletRequest request, final String jwtRequest) {
        val unpackedRequest = request.getAttribute(UnpackedJwtRequest.class.getName());
        if (unpackedRequest instanceof UnpackedJwtRequest(String requestObject, JWTClaimsSet claims) && requestObject.equals(jwtRequest)) {
            return claims;
        }
        val id = Objects.requireNonNullElse(request.getParameter(OAuth20Constants.CLIENT_ID), StringUtils.EMPTY);
        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(jwtBuilder.getServicesManager(), id);
        val jwt = jwtBuilder.unpack(Optional.ofNullable(service), jwtRequest);
        if (jwt != null) {
            request.setAttribute(UnpackedJwtRequest.class.getName(), new UnpackedJwtRequest(jwtRequest, jwt));
        }
        return jwt;
    }

    @Override
    public Map<String, Set<String>> resolveRequestParameters(final Collection<String> attributes,
                                                             final HttpServletRequest request) {
        return attributes
            .stream()
            .map(name -> {
                val values = resolveRequestParameter(request, name)
                    .map(EncodingUtils::urlDecode)
                    .map(value -> Arrays.stream(value.split(" ")).collect(Collectors.toSet()))
                    .orElseGet(Set::of);
                return Pair.of(name, values);
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    public Set<String> resolveRequestParameters(final HttpServletRequest request, final String name) {
        return resolveRequestParameters(List.of(name), request).getOrDefault(name, Set.of());
    }

    @Override
    public Optional<String> resolveRequestParameter(final HttpServletRequest request,
                                                    final String name) {
        return resolveRequestParameter(request, name, String.class);
    }

    @Override
    public <T> Optional<T> resolveRequestParameter(final HttpServletRequest request,
                                                   final String name,
                                                   final Class<T> clazz) {
        val supported = isRequestParameterSupported(request);
        return Optional.ofNullable(request.getParameter(OAuth20Constants.REQUEST))
            .filter(parameterValue -> supported)
            .map(Unchecked.function(jwtRequest -> resolveJwtRequestParameter(request, jwtRequest, name, clazz)))
            .or(() -> {
                val values = request.getParameterMap().get(name);
                if (values != null && values.length > 0) {
                    if (clazz.isArray()) {
                        return Optional.of(clazz.cast(values));
                    }
                    if (Collection.class.isAssignableFrom(clazz)) {
                        return Optional.of(clazz.cast(CollectionUtils.wrapArrayList(values)));
                    }
                    val singleValue = EncodingUtils.urlDecode(values[0]);
                    if (Long.class.isAssignableFrom(clazz)){
                        return Optional.ofNullable(singleValue).map(Long::parseLong).map(clazz::cast);
                    }
                    if (Integer.class.isAssignableFrom(clazz)){
                        return Optional.ofNullable(singleValue).map(Integer::parseInt).map(clazz::cast);
                    }
                    if (Double.class.isAssignableFrom(clazz)){
                        return Optional.ofNullable(singleValue).map(Double::parseDouble).map(clazz::cast);
                    }
                    return Optional.ofNullable(singleValue)
                        .filter(StringUtils::isNotBlank)
                        .map(clazz::cast);
                }
                return Optional.empty();
            });
    }
    

    @Override
    public Collection<String> resolveRequestedScopes(final HttpServletRequest request) {
        val map = resolveRequestParameters(CollectionUtils.wrap(OAuth20Constants.SCOPE), request);
        if (map == null || map.isEmpty()) {
            return new HashSet<>();
        }
        val supported = getSupportedScopes(request);
        val results = new LinkedHashSet<>(map.get(OAuth20Constants.SCOPE));
        if (!supported.contains("*")) {
            results.retainAll(supported);
        }
        return results;
    }

    @Override
    public Set<String> resolveRequestedResources(final HttpServletRequest request) {
        return resolveRequestParameter(request, OAuth20Constants.RESOURCE, String[].class)
            .map(Arrays::stream)
            .orElseGet(Stream::empty)
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override
    public boolean isAuthorizedResourceForService(final HttpServletRequest request,
                                                  final OAuthRegisteredService registeredService) {
        val allowedResources = resolveAllowedResources(registeredService);
        return resolveRequestedResources(request).stream().allMatch(resource -> {
            if (RegisteredServiceOAuthResourceMatcher.isValidResource(resource)
                && RegisteredServiceOAuthResourceMatcher.isResourceAllowed(allowedResources, resource)) {
                return true;
            }
            LOGGER.debug("Resource [{}] is invalid or not allowed for service [{}]", resource, registeredService.getName());
            return false;
        });
    }

    @Override
    public Set<String> resolveAllowedResources(final OAuthRegisteredService registeredService) {
        val allowedResources = new LinkedHashSet<String>(registeredService.getAllowedResources());
        allowedResources.addAll(jwtBuilder.getCasProperties().getAuthn().getOauth().getCore().getAllowedResources());
        return allowedResources;
    }

    @Override
    public boolean isAuthorizedGrantTypeForService(final HttpServletRequest request,
                                                   final OAuthRegisteredService registeredService) {
        val grantType = resolveRequestParameter(request, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20RequestParameterResolver.isAuthorizedGrantTypeForService(grantType, registeredService);
    }

    @Override
    public boolean isAuthorizedResponseTypeForService(final HttpServletRequest request,
                                                      final OAuthRegisteredService registeredService) {
        if (registeredService.getSupportedResponseTypes() != null && !registeredService.getSupportedResponseTypes().isEmpty()) {
            val responseType = resolveRequestParameter(request, OAuth20Constants.RESPONSE_TYPE)
                .map(String::valueOf).orElse(StringUtils.EMPTY);
            if (registeredService.getSupportedResponseTypes().stream().anyMatch(s -> s.equalsIgnoreCase(responseType))) {
                return true;
            }
            LOGGER.warn("Response type not authorized for service: [{}] not listed in supported response types: [{}]",
                responseType, registeredService.getSupportedResponseTypes());
            return false;
        }
        LOGGER.warn("Registered service [{}] does not define any authorized/supported response types. "
            + "It is STRONGLY recommended that you authorize and assign response types to the service definition. "
            + "While just a warning for now, this behavior will be enforced by CAS in future versions.", registeredService.getName());
        return true;
    }

    @Override
    public Pair<String, String> resolveClientIdAndClientSecret(final HttpServletRequest request) {
        val basicCredentials = resolveBasicAuthorizationCredentials(request);
        if (basicCredentials.isPresent()) {
            return basicCredentials.get();
        }
        val clientId = resolveRequestParameter(request, OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientSecret = resolveRequestParameter(request, OAuth20Constants.CLIENT_SECRET)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return Pair.of(clientId, clientSecret);
    }

    /**
     * Read the client id and secret of the HTTP Basic {@code Authorization} header, form-url-decoded (RFC 6749, section 2.3.1).
     * A header that is not a Basic one, or that cannot be decoded, fails with a {@link BadCredentialsException}.
     *
     * @param request the request
     * @return the client id and secret, if the request carries the header
     */
    protected Optional<Pair<String, String>> resolveBasicAuthorizationCredentials(final HttpServletRequest request) {
        return OAuth20Utils.readClientBasicAuthorization(request);
    }

    @Override
    public Set<String> resolveRequestScopes(final HttpServletRequest request) {
        val parameterValues = resolveRequestParameter(request, OAuth20Constants.SCOPE);
        if (parameterValues.isEmpty()) {
            return new HashSet<>();
        }
        val supported = getSupportedScopes(request);
        val results = CollectionUtils.wrapSet(parameterValues.get().split(" "));
        if (!supported.contains("*")) {
            results.retainAll(supported);
        }
        return results;
    }

    @Override
    public Map<String, Map<String, Object>> resolveRequestClaims(final HttpServletRequest request) {
        val supported = isClaimsParameterSupported(request);

        val claims = FunctionUtils.doIf(supported,
            () -> resolveRequestParameter(request, OAuth20Constants.CLAIMS).map(String::valueOf).orElse(StringUtils.EMPTY),
            () -> StringUtils.EMPTY).get();

        if (StringUtils.isBlank(claims)) {
            return new HashMap<>();
        }
        return MAPPER.readValue(JsonValue.readHjson(claims).toString(), Map.class);
    }
    

    @Override
    public Set<String> resolveUserInfoRequestClaims(final HttpServletRequest request) {
        val requestedClaims = resolveRequestClaims(request);
        val userInfo = requestedClaims.getOrDefault(OAuth20Constants.CLAIMS_USERINFO, new HashMap<>());
        return new TreeSet<>(userInfo.keySet());
    }

    /**
     * The prompt values of the request: those of its query, and those of its form body, since an authorization request may
     * be a {@code POST} (OpenID Connect Core, section 3.1.2.1). A {@code POST} that goes to the login is saved as a
     * {@code GET} with its parameters, so the callback sees, and removes after a fresh login, the same values. A
     * {@code prompt} inside a request object is not read: the URL of the saved request cannot be rewritten to drop it after a
     * fresh login, so honoring it would send the browser back to the login without end.
     *
     * @param request the request
     * @return the prompt values
     */
    @Override
    public Set<String> resolveRequestedPromptValues(final HttpServletRequest request) {
        val values = new LinkedHashSet<>(readPromptValues(HttpRequestUtils.getFullRequestUrl(request)));
        Optional.ofNullable(request.getParameterValues(OAuth20Constants.PROMPT))
            .stream()
            .flatMap(Arrays::stream)
            .map(value -> StringUtils.split(value, ' '))
            .flatMap(Arrays::stream)
            .forEach(values::add);
        return values;
    }

    @Override
    public Set<String> resolveSupportedPromptValues(final String url) {
        return filterSupportedPromptValues(readPromptValues(url));
    }

    @Override
    public Set<String> resolveSupportedPromptValues(final HttpServletRequest request) {
        return filterSupportedPromptValues(resolveRequestedPromptValues(request));
    }

    private Set<String> filterSupportedPromptValues(final Collection<String> values) {
        val supported = getPromptValuesSupported();
        return values
            .stream()
            .filter(value -> supported.stream().anyMatch(supportedValue -> "*".equalsIgnoreCase(supportedValue) || supportedValue.equalsIgnoreCase(value)))
            .collect(Collectors.toSet());
    }

    private static Set<String> readPromptValues(final String url) {
        return FunctionUtils.doUnchecked(() -> new URIBuilder(url).setPlusAsBlank(true).getQueryParams()
            .stream()
            .filter(param -> OAuth20Constants.PROMPT.equals(param.getName()))
            .map(param -> StringUtils.split(Objects.toString(param.getValue(), StringUtils.EMPTY), ' '))
            .flatMap(Arrays::stream)
            .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    @Override
    public boolean isParameterOnQueryString(final HttpServletRequest request, final String name) {
        val queryString = request.getQueryString();
        return queryString != null && request.getParameter(name) != null && queryString.contains(name + '=');
    }

    protected List<String> getSupportedScopes(final HttpServletRequest request) {
        return List.of("*");
    }
    
    protected List<String> getResponseTypesSupported(final HttpServletRequest request) {
        return List.of("*");
    }

    protected List<String> getGrantTypesSupported(final HttpServletRequest request) {
        return List.of("*");
    }

    protected List<String> getResponseModesSupported(final HttpServletRequest request) {
        return List.of("*");
    }

    protected boolean isRequestParameterSupported(final HttpServletRequest request) {
        return true;
    }

    protected boolean isClaimsParameterSupported(final HttpServletRequest request) {
        return true;
    }

    protected List<String> getPromptValuesSupported() {
        return List.of("*");
    }

    private static <T> @Nullable T getJwtRequestClaim(final JWTClaimsSet jwt, final String name, final Class<T> clazz) throws Exception {
        if (clazz.isArray()) {
            val values = getJwtRequestClaimValues(jwt, name);
            return values == null ? null : clazz.cast(values.toArray(String[]::new));
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return clazz.cast(getJwtRequestClaimValues(jwt, name));
        }
        return clazz.cast(jwt.getStringClaim(name));
    }

    /**
     * Read a multi-valued request object claim. A single string is accepted as one value,
     * i.e. {@code "resource": "https://api.example.org/"}, since such claims may carry either form.
     *
     * @param jwt  the request object claims
     * @param name the claim name
     * @return the values, or null if the claim is absent
     * @throws ParseException if the claim is neither a string nor a list of strings
     */
    private static @Nullable List<String> getJwtRequestClaimValues(final JWTClaimsSet jwt, final String name) throws ParseException {
        return jwt.getClaim(name) instanceof final String value ? List.of(value) : jwt.getStringListClaim(name);
    }

    private record UnpackedJwtRequest(String jwtRequest, JWTClaimsSet claims) {
    }
}
