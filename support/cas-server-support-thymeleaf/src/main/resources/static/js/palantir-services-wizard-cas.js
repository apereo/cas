function handleRegisteredServiceProxyPolicyChange(select) {
    const type = getLastWord($(select).val());
    $(`#editServiceWizardMenuItemProxyAuthenticationPolicy .${type}`).show();
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='FieldContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='MapContainer']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='ButtonPanel']")
        .not(`.${type}`)
        .hide();
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='FieldContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='MapContainer'] input")
        .val("");
    $("#editServiceWizardMenuItemProxyAuthenticationPolicy [id$='ButtonPanel'] button")
        .each(function () {
            const selected = $(this).data("param-selected");
            if (selected) {
                $(this).click();
            }
        });
}

function createRegisteredServiceProxyPolicy() {
    createSelectField({
        containerId: "editServiceWizardMenuItemProxyAuthenticationPolicy",
        labelTitle: "Type:",
        paramName: "proxyPolicy",
        options: [
            {
                value: "org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy",
                text: "REFUSE",
                selected: true
            },
            {
                value: "org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy",
                text: "REGEX"
            },
            {
                value: "org.apereo.cas.services.RestfulRegisteredServiceProxyPolicy",
                text: "REST"
            }

        ],
        helpText: "Specifies the strategy to determine the username attribute.",
        changeEventHandlers: "handleRegisteredServiceProxyPolicyChange"
    })
        .data("renderer", function (value) {
            return {"@class": value};
        });

    createInputField({
        cssClasses: "hide RegexMatchingRegisteredServiceProxyPolicy",
        labelTitle: "Pattern",
        name: "registeredServiceProxyPolicyRegexPattern",
        paramName: "proxyPolicy.pattern",
        required: false,
        containerId: "editServiceWizardMenuItemProxyAuthenticationPolicy",
        title: "Specifies the URL pattern that is allowed for proxy authentication"
    });

    createInputField({
        cssClasses: "hide RestfulRegisteredServiceProxyPolicy",
        labelTitle: "Endpoint",
        name: "registeredServiceProxyPolicyRestEndpoint",
        paramName: "proxyPolicy.endpoint",
        required: false,
        containerId: "editServiceWizardMenuItemProxyAuthenticationPolicy",
        title: "Specifies the endpoint CAS must reach to determine proxy authentication eligibility"
    });

    createMappedInputField({
        cssClasses: "hide RestfulRegisteredServiceProxyPolicy",
        containerId: "editServiceWizardMenuItemProxyAuthenticationPolicy",
        keyField: "proxyPolicyHeaderName",
        keyLabel: "Header Name",
        valueField: "proxyPolicyHeaderValue",
        valueLabel: "Header Value",
        containerField: "proxyPolicy.headers"
    });

}

function createRegisteredServiceProxyTicketPolicy() {
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy",
        labelTitle: "Number of Uses",
        name: "registeredServiceProxyTicketExpirationPolicyNumberOfUses",
        paramName: "proxyTicketExpirationPolicy.numberOfUses",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemPTExpirationPolicy",
        title: "Control the number of times the ticket can be used"
    });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy",
        labelTitle: "Time to Live",
        name: "registeredServiceProxyTicketExpirationPolicyTimeToLive",
        paramName: "proxyTicketExpirationPolicy.timeToLive",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemPTExpirationPolicy",
        title: "Control how long the ticket should be valid, in seconds."
    });
}

function createRegisteredServiceProxyGrantingTicketPolicy() {
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceProxyTicketExpirationPolicy",
        labelTitle: "Max Time to Live",
        name: "registeredServiceProxyGrantingTicketExpirationPolicyMaxTimeToLive",
        paramName: "proxyGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemPGTExpirationPolicy",
        title: "Control how long the ticket should be valid, in seconds."
    });
}

function createRegisteredServiceServiceTicketPolicy() {
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy",
        labelTitle: "Number of Uses",
        name: "registeredServiceTicketExpirationPolicyNumberOfUses",
        paramName: "serviceTicketExpirationPolicy.numberOfUses",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemSTExpirationPolicy",
        title: "Control the number of times the ticket can be used"
    });

    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceServiceTicketExpirationPolicy",
        labelTitle: "Time to Live",
        name: "registeredServiceTicketExpirationPolicyTimeToLive",
        paramName: "serviceTicketExpirationPolicy.timeToLive",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemSTExpirationPolicy",
        title: "Control how long the ticket should be valid, in seconds."
    });
}

function createRegisteredServiceTicketGrantingTicketPolicy() {
    createInputField({
        paramType: "org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy",
        labelTitle: "Max Time to Live",
        name: "registeredServiceTicketGrantingTicketExpirationPolicyMaxTimeToLive",
        paramName: "ticketGrantingTicketExpirationPolicy.maxTimeToLiveInSeconds",
        required: false,
        dataType: "number",
        containerId: "editServiceWizardMenuItemTGTExpirationPolicy",
        title: "Control how long the ticket should be valid, in seconds."
    });

    createMappedInputField({
        header: "User Agents",
        containerType: "org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy",
        containerId: "editServiceWizardMenuItemTGTExpirationPolicy",
        keyField: "registeredServiceTGTExpirationPolicyUserAgentName",
        keyLabel: "User Agent",
        valueField: "registeredServiceTGTExpirationPolicyUserAgentExpirationValue",
        valueFieldType: "number",
        valueLabel: "Max Time to Live (in seconds)",
        containerField: "ticketGrantingTicketExpirationPolicy.userAgents"
    });

    createMappedInputField({
        header: "IP Addresses",
        containerType: "org.apereo.cas.services.DefaultRegisteredServiceTicketGrantingTicketExpirationPolicy",
        containerId: "editServiceWizardMenuItemTGTExpirationPolicy",
        keyField: "registeredServiceTGTExpirationPolicyIPName",
        keyLabel: "IP Address",
        valueField: "registeredServiceTGTExpirationPolicyIPExpirationValue",
        valueLabel: "Max Time to Live (in seconds)",
        valueFieldType: "number",
        containerField: "ticketGrantingTicketExpirationPolicy.ipAddresses"
    });
}

function createCasRegisteredServiceFields() {
    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardGeneralContainer",
        labelTitle: "Response Type:",
        paramName: "responseType",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {value: "POST", text: "POST"},
            {value: "REDIRECT", text: "REDIRECT"},
            {value: "HEADER", text: "HEADER"}
        ],
        helpText: "Specify the response type for this application. <code>POST</code> indicates form post, " +
            "<code>REDIRECT</code> indicates HTTP redirect, and <code>HEADER</code> indicates HTTP header-based response."
    });

    createSelectField({
        cssClasses: "advanced-option",
        containerId: "editServiceWizardGeneralContainer",
        labelTitle: "Logout Type:",
        paramName: "logoutType",
        options: [
            {value: "", text: "DEFAULT", selected: true},
            {value: "BACK_CHANNEL", text: "BACK_CHANNEL"},
            {value: "FRONT_CHANNEL", text: "FRONT_CHANNEL"},
            {value: "NONE", text: "NONE"}
        ],
        helpText: "Specify the logout type for this application. <code>BACK_CHANNEL</code> indicates server-to-server logout, " +
            "<code>FRONT_CHANNEL</code> indicates browser-based logout, and <code>NONE</code> indicates no logout handling."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Redirect URL",
        name: "registeredServiceRedirectUrl",
        paramName: "redirectUrl",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the URL to which users are redirected after successful authentication for this application."
    });
    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Supported Protocol(s)",
        name: "registeredServiceSupportedProtocols",
        paramName: "supportedProtocols",
        required: false,
        containerId: "editServiceWizardGeneralContainer",
        title: "Define the supported protocol(s) for this application, separated by comma (e.g., <code>CAS10,CAS20,CAS30,SAML1</code>)."
    })
        .data("renderer", function (value) {
            return ["java.util.HashSet", value.split(",")];
        });
}
