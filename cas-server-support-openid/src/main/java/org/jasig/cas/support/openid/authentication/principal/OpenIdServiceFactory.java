package org.jasig.cas.support.openid.authentication.principal;

import org.jasig.cas.authentication.principal.AbstractServiceFactory;
import org.jasig.cas.support.openid.OpenIdProtocolConstants;
import org.openid4java.message.ParameterList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotNull;

/**
 * The {@link OpenIdServiceFactory} creates {@link OpenIdService} objects.
 *
 * @author Misagh Moayyed
 * @since 4.2
 */
@Component("openIdServiceFactory")
public class OpenIdServiceFactory extends AbstractServiceFactory<OpenIdService> {

    /**
     * The prefix url for OpenID (without the trailing slash).
     */
    @NotNull
    @Value("${server.prefix}/openid")
    private String openIdPrefixUrl;

    @Autowired
    private ApplicationContext applicationContext;

    public String getOpenIdPrefixUrl() {
        return openIdPrefixUrl;
    }

    public void setOpenIdPrefixUrl(final String openIdPrefixUrl) {
        this.openIdPrefixUrl = openIdPrefixUrl;
    }

    @Override
    public OpenIdService createService(final HttpServletRequest request) {
        final String service = request.getParameter(OpenIdProtocolConstants.OPENID_RETURNTO);
        final String openIdIdentity = request.getParameter(OpenIdProtocolConstants.OPENID_IDENTITY);

        if (openIdIdentity == null || !StringUtils.hasText(service)) {
            return null;
        }

        final String id = cleanupUrl(service);
        final String artifactId = request.getParameter(OpenIdProtocolConstants.OPENID_ASSOCHANDLE);
        final ParameterList paramList = new ParameterList(request.getParameterMap());


        final OpenIdServiceResponseBuilder builder = new OpenIdServiceResponseBuilder(
                paramList, this.openIdPrefixUrl);

        return new OpenIdService(id, service, artifactId, openIdIdentity, builder);
    }

    @Override
    public OpenIdService createService(final String id) {
        final ParameterList paramList = new ParameterList();
        final OpenIdServiceResponseBuilder builder = new OpenIdServiceResponseBuilder(
                paramList, this.openIdPrefixUrl);
        return new OpenIdService(id, id, null, this.openIdPrefixUrl, builder);
    }
}
