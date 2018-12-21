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

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

/**
 * This is {@link WSFederationRegisteredService}.
 *
 * @author Misagh Moayyed
 * @since 5.1.0
 */
@Entity
@DiscriminatorValue("wsfed")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WSFederationRegisteredService extends RegexRegisteredService {

    private static final long serialVersionUID = -3700571300568534062L;

    @Column
    private String realm = WSFederationConstants.REALM_DEFAULT_URI;

    @Column
    private String protocol = WSFederationConstants.WST_NS_05_12;

    @Column
    private String tokenType = WSFederationConstants.WSS_SAML2_TOKEN_TYPE;

    @Column(length = 512)
    private String wsdlLocation;

    @Column(length = 512)
    private String namespace = WSFederationConstants.HTTP_DOCS_OASIS_OPEN_ORG_WS_SX_WS_TRUST_200512;

    @Column(length = 512)
    private String addressingNamespace = WSFederationConstants.HTTP_WWW_W3_ORG_2005_08_ADDRESSING;

    @Column(length = 512)
    private String policyNamespace;

    @Column(length = 512)
    private String wsdlService = WSFederationConstants.SECURITY_TOKEN_SERVICE;

    @Column(length = 512)
    private String wsdlEndpoint = WSFederationConstants.SECURITY_TOKEN_SERVICE_ENDPOINT;

    @Column(length = 512)
    private String appliesTo;

    @Override
    protected AbstractRegisteredService newInstance() {
        return new WSFederationRegisteredService();
    }

    @JsonIgnore
    @Override
    public String getFriendlyName() {
        return "WS Federation Relying Party";
    }
}
