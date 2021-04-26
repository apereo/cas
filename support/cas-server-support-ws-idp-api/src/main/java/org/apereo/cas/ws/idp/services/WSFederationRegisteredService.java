package org.apereo.cas.ws.idp.services;

import org.apereo.cas.services.AbstractRegisteredService;
import org.apereo.cas.services.RegexRegisteredService;
import org.apereo.cas.ws.idp.WSFederationConstants;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * This is {@link WSFederationRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WSFederationRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = -3700571300568534062L;

    private String realm = WSFederationConstants.REALM_DEFAULT_URI;

    private String protocol = WSFederationConstants.WST_NS_05_12;

    private String tokenType = WSFederationConstants.WSS_SAML2_TOKEN_TYPE;

    private String wsdlLocation;

    private String namespace = WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512;

    private String addressingNamespace = WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING;

    private String policyNamespace;

    private String wsdlService = WSFederationConstants.SECURITY_TOKEN_SERVICE;

    private String wsdlEndpoint = WSFederationConstants.SECURITY_TOKEN_SERVICE_ENDPOINT;

    private String appliesTo;

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "WS Federation Relying Party";
    }

    @JsonIgnore
    @Override
    public int getEvaluationPriority() {
        return 3;
    }

    @Override
    protected AbstractRegisteredService newInstance() {
        return new WSFederationRegisteredService();
    }
}
