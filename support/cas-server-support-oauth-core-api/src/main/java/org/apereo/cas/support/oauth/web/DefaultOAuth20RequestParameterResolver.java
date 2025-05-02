package org.apereo.cas.support.oauth.web;

import org.apereo.cas.services.RegisteredService;
import org.apereo.cas.support.oauth.OAuth20Constants;
import org.apereo.cas.support.oauth.OAuth20GrantTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseModeTypes;
import org.apereo.cas.support.oauth.OAuth20ResponseTypes;
import org.apereo.cas.support.oauth.services.OAuthRegisteredService;
import org.apereo.cas.support.oauth.util.OAuth20Utils;
import org.apereo.cas.token.JwtBuilder;
import org.apereo.cas.util.CollectionUtils;
import org.apereo.cas.util.EncodingUtils;
import org.apereo.cas.util.function.FunctionUtils;
import org.apereo.cas.util.serialization.JacksonObjectMapperFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.hc.core5.net.URIBuilder;
import org.hjson.JsonValue;
import org.jooq.lambda.Unchecked;
import org.pac4j.core.context.CallContext;
import org.pac4j.core.context.WebContext;
import org.pac4j.core.context.WebContextHelper;
import org.pac4j.core.credentials.UsernamePasswordCredentials;
import org.pac4j.core.credentials.extractor.BasicAuthExtractor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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

    private final JwtBuilder jwtBuilder;

    @Override
    public OAuth20ResponseTypes resolveResponseType(final WebContext context) {
        val responseTypesSupport = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseTypesSupported();
        val responseType = resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
            .map(String::valueOf)
            .filter(typeName -> responseTypesSupport.stream().anyMatch(supported -> supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseTypes.CODE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }

    @Override
    public OAuth20GrantTypes resolveGrantType(final WebContext context) {
        val grantTypesSupport = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getGrantTypesSupported();
        val grantType = resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf)
            .filter(typeName -> grantTypesSupport.stream().anyMatch(supported -> supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20GrantTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(grantType))
            .findFirst()
            .orElse(OAuth20GrantTypes.NONE);
        LOGGER.debug("OAuth grant type is [{}]", type);
        return type;
    }

    @Override
    public OAuth20ResponseModeTypes resolveResponseModeType(final WebContext context) {
        val supportedResponseModes = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getResponseModesSupported();
        val responseType = resolveRequestParameter(context, OAuth20Constants.RESPONSE_MODE)
            .map(String::valueOf)
            .filter(typeName -> supportedResponseModes.stream().anyMatch(supported -> supported.equalsIgnoreCase(typeName)))
            .orElse(StringUtils.EMPTY);
        val type = Arrays.stream(OAuth20ResponseModeTypes.values())
            .filter(t -> t.getType().equalsIgnoreCase(responseType))
            .findFirst()
            .orElse(OAuth20ResponseModeTypes.NONE);
        LOGGER.debug("OAuth response type is [{}]", type);
        return type;
    }

    @Override
    public <T> T resolveJwtRequestParameter(final String jwtRequest, final RegisteredService registeredService,
                                            final String name, final Class<T> clazz) throws Exception {
        val jwt = jwtBuilder.unpack(Optional.ofNullable(registeredService), jwtRequest);
        if (clazz.isArray()) {
            return clazz.cast(jwt.getStringArrayClaim(name));
        }
        if (Collection.class.isAssignableFrom(clazz)) {
            return clazz.cast(jwt.getStringListClaim(name));
        }
        return clazz.cast(jwt.getStringClaim(name));
    }

    @Override
    public <T> T resolveJwtRequestParameter(final WebContext context, final String jwtRequest,
                                            final String name, final Class<T> clazz) {
        val id = context.getRequestParameter(OAuth20Constants.CLIENT_ID).orElse(StringUtils.EMPTY);
        val service = OAuth20Utils.getRegisteredOAuthServiceByClientId(jwtBuilder.getServicesManager(), id);
        return FunctionUtils.doUnchecked(() -> resolveJwtRequestParameter(jwtRequest, service, name, clazz));
    }

    @Override
    public Map<String, Set<String>> resolveRequestParameters(final Collection<String> attributes,
                                                             final WebContext context) {
        return attributes
            .stream()
            .map(name -> {
                val values = resolveRequestParameter(context, name)
                    .map(EncodingUtils::urlDecode)
                    .map(value -> Arrays.stream(value.split(" ")).collect(Collectors.toSet()))
                    .orElseGet(Set::of);
                return Pair.of(name, values);
            })
            .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @Override
    public Set<String> resolveRequestParameters(final WebContext context, final String name) {
        return resolveRequestParameters(List.of(name), context).getOrDefault(name, Set.of());
    }

    @Override
    public Optional<String> resolveRequestParameter(final WebContext context,
                                                    final String name) {
        return resolveRequestParameter(context, name, String.class);
    }

    @Override
    public <T> Optional<T> resolveRequestParameter(final WebContext context,
                                                   final String name,
                                                   final Class<T> clazz) {
        val supported = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isRequestParameterSupported();
        return context.getRequestParameter(OAuth20Constants.REQUEST)
            .filter(parameterValue -> supported)
            .map(Unchecked.function(jwtRequest -> resolveJwtRequestParameter(context, jwtRequest, name, clazz)))
            .or(() -> {
                val values = context.getRequestParameters().get(name);
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
                    return Optional.ofNullable(singleValue).map(clazz::cast);
                }
                return Optional.empty();
            });
    }

    @Override
    public Collection<String> resolveRequestedScopes(final WebContext context) {
        val map = resolveRequestParameters(CollectionUtils.wrap(OAuth20Constants.SCOPE), context);
        if (map == null || map.isEmpty()) {
            return new ArrayList<>();
        }
        val supported = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getScopes();
        val results = new LinkedHashSet<>(map.get(OAuth20Constants.SCOPE));
        results.retainAll(supported);
        return results;
    }

    @Override
    public boolean isAuthorizedGrantTypeForService(final WebContext context,
                                                   final OAuthRegisteredService registeredService) {
        val grantType = resolveRequestParameter(context, OAuth20Constants.GRANT_TYPE)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return OAuth20RequestParameterResolver.isAuthorizedGrantTypeForService(grantType, registeredService);
    }

    @Override
    public boolean isAuthorizedResponseTypeForService(final WebContext context,
                                                      final OAuthRegisteredService registeredService) {
        if (registeredService.getSupportedResponseTypes() != null && !registeredService.getSupportedResponseTypes().isEmpty()) {
            val responseType = resolveRequestParameter(context, OAuth20Constants.RESPONSE_TYPE)
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
    public Pair<String, String> resolveClientIdAndClientSecret(final CallContext callContext) {
        val extractor = new BasicAuthExtractor();
        val upcResult = extractor.extract(callContext);
        if (upcResult.isPresent()) {
            val upc = (UsernamePasswordCredentials) upcResult.get();
            return Pair.of(upc.getUsername(), upc.getPassword());
        }
        val clientId = resolveRequestParameter(callContext.webContext(), OAuth20Constants.CLIENT_ID)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        val clientSecret = resolveRequestParameter(callContext.webContext(), OAuth20Constants.CLIENT_SECRET)
            .map(String::valueOf).orElse(StringUtils.EMPTY);
        return Pair.of(clientId, clientSecret);
    }

    @Override
    public Set<String> resolveRequestScopes(final WebContext context) {
        val parameterValues = resolveRequestParameter(context, OAuth20Constants.SCOPE);
        if (parameterValues.isEmpty()) {
            return new HashSet<>(0);
        }
        val supported = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getScopes();
        val results = CollectionUtils.wrapSet(parameterValues.get().split(" "));
        results.retainAll(supported);
        return results;
    }

    @Override
    public Map<String, Map<String, Object>> resolveRequestClaims(final WebContext context) throws Exception {
        val supported = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().isClaimsParameterSupported();

        val claims = FunctionUtils.doIf(supported,
            () -> resolveRequestParameter(context, OAuth20Constants.CLAIMS).map(String::valueOf).orElse(StringUtils.EMPTY),
            () -> StringUtils.EMPTY).get();

        if (StringUtils.isBlank(claims)) {
            return new HashMap<>();
        }
        return MAPPER.readValue(JsonValue.readHjson(claims).toString(), Map.class);
    }

    @Override
    public Set<String> resolveUserInfoRequestClaims(final WebContext context) throws Exception {
        val requestedClaims = resolveRequestClaims(context);
        return requestedClaims.getOrDefault(OAuth20Constants.CLAIMS_USERINFO, new HashMap<>()).keySet();
    }

    @Override
    public Set<String> resolveRequestedPromptValues(final WebContext context) {
        val url = context.getFullRequestURL();
        return FunctionUtils.doUnchecked(() -> new URIBuilder(url).getQueryParams()
            .stream()
            .filter(p -> OAuth20Constants.PROMPT.equals(p.getName()))
            .map(param -> param.getValue().split(" "))
            .flatMap(Arrays::stream)
            .collect(Collectors.toSet()));
    }

    @Override
    public Set<String> resolveSupportedPromptValues(final String url) {
        val supported = jwtBuilder.getCasProperties().getAuthn().getOidc().getDiscovery().getPromptValuesSupported();
        return FunctionUtils.doUnchecked(() -> new URIBuilder(url).getQueryParams()
            .stream()
            .filter(p -> OAuth20Constants.PROMPT.equals(p.getName()))
            .map(param -> param.getValue().split(" "))
            .flatMap(Arrays::stream)
            .filter(supported::contains)
            .collect(Collectors.toSet()));
    }

    @Override
    public boolean isParameterOnQueryString(final WebContext context, final String name) {
        return WebContextHelper.isQueryStringParameter(context, name);
    }
}
