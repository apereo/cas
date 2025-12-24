package org.apereo.cas.authentication.metadata;

import module java.base;
import org.apereo.cas.authentication.AuthenticationBuilder;
import org.apereo.cas.authentication.AuthenticationManager;
import org.apereo.cas.authentication.AuthenticationTransaction;
import org.apereo.cas.authentication.Credential;
import org.apereo.cas.util.function.FunctionUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.apereo.inspektr.common.web.ClientInfoHolder;
import org.jspecify.annotations.Nullable;


/**
 * This is {@link ClientInfoAuthenticationMetaDataPopulator}.
 *
 * @author Misagh Moayyed
 * @since 5.2.0
 */
public class ClientInfoAuthenticationMetaDataPopulator extends BaseAuthenticationMetaDataPopulator {

    /**
     * Attribute to track client ip address.
     */
    public static final String ATTRIBUTE_CLIENT_IP_ADDRESS = "clientIpAddress";

    /**
     * Attribute to track server ip address.
     */
    public static final String ATTRIBUTE_SERVER_IP_ADDRESS = "serverIpAddress";

    /**
     * Attribute to track user-agent, if any.
     */
    public static final String ATTRIBUTE_USER_AGENT = "userAgent";

    /**
     * Attribute to track geo location, if any.
     */
    public static final String ATTRIBUTE_GEO_LOCATION = "geoLocation";

    private static void addAttribute(final AuthenticationBuilder builder, final String name, final @Nullable String value) {
        FunctionUtils.doIf(StringUtils.isNotBlank(value), v -> builder.mergeAttribute(name, v)).accept(value);
    }

    @Override
    public void populateAttributes(final AuthenticationBuilder builder, final AuthenticationTransaction transaction) {
        val clientInfo = ClientInfoHolder.getClientInfo();
        if (clientInfo != null) {
            addAttribute(builder, ATTRIBUTE_CLIENT_IP_ADDRESS, clientInfo.getClientIpAddress());
            addAttribute(builder, ATTRIBUTE_SERVER_IP_ADDRESS, clientInfo.getServerIpAddress());
            addAttribute(builder, ATTRIBUTE_USER_AGENT, clientInfo.getUserAgent());
            addAttribute(builder, ATTRIBUTE_GEO_LOCATION, clientInfo.getGeoLocation());
            addAttribute(builder, AuthenticationManager.TENANT_ID_ATTRIBUTE, clientInfo.getTenant());
        }
    }

    @Override
    public boolean supports(final Credential credential) {
        return credential != null && ClientInfoHolder.getClientInfo() != null;
    }
}
