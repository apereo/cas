function createRegisteredServiceWsfedOptions() {
    createInputField({
        labelTitle: "Realm",
        name: "registeredServiceWsFederationRealm",
        paramName: "realm",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Federation realm for the relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Protocol",
        name: "registeredServiceWsFederationProtocol",
        paramName: "protocol",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Federation protocol to be used (e.g., <code>http://docs.oasis-open.org/ws-sx/ws-trust/200512</code>)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Token Type",
        name: "registeredServiceWsFederationTokenType",
        paramName: "tokenType",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Federation token type to be used (e.g., <code>http://docs.oasis-open.org/wss/oasis-wss-saml-token-profile-1.1#SAMLV2.0</code>)."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "WSDL Location",
        name: "registeredServiceWsFederationWsdlLocation",
        paramName: "wsdlLocation",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Specifies the location of the WSDL for the WS-Federation relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Namespace",
        name: "registeredServiceWsFederationNamespace",
        paramName: "namespace",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Federation namespace for the relying party (i.e. <code>http://docs.oasis-open.org/ws-sx/ws-trust/200512/</code>"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Addressing Namespace",
        name: "registeredServiceWsFederationAddressingNamespace",
        paramName: "addressingNamespace",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Addressing namespace for the relying party (i.e. <code>http://www.w3.org/2005/08/addressing</code>)"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Policy Namespace",
        name: "registeredServiceWsFederationPolicyNamespace",
        paramName: "policyNamespace",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Defines the WS-Policy namespace for the relying party."
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "WSDL Service",
        name: "registeredServiceWsFederationWsdlService",
        paramName: "wsdlService",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Specifies the WSDL service name for the WS-Federation relying party (i.e. <code>SecurityTokenService</code>)"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "WSDL Endpoint",
        name: "registeredServiceWsFederationWsdlEndpoint",
        paramName: "wsdlEndpoint",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Specifies the WSDL endpoint name for the WS-Federation relying party"
    });

    createInputField({
        cssClasses: "advanced-option",
        labelTitle: "Applies To",
        name: "registeredServiceWsFederationAppliesTo",
        paramName: "appliesTo",
        required: false,
        containerId: "editServiceWizardWSFederationContainer",
        title: "Controls to whom security tokens apply. Defaults to the realm value."
    });
}
