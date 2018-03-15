export class FormData {
    availableAttributes: String[] = [];
    registeredServiceProperties: PropertyEnum[];
    grouperFields: String[];
    remoteCodes: String[];
    timeUnits: String[];
    mergingStrategies: String[];
    logoutTypes: String[];
    serviceTypes: String[];
    samlRoles: String[];
    samlDirections: String[];
    samlAttributeNameFormats: String[];
    samlCredentialTypes: String[];
    wsFederationClaims: String[];
    mfaProviders: PropertyEnum[];
    mfaFailureModes: String[];
    oidcScopes: PropertyEnum[];
    oidcEncodingAlgOptions: PropertyEnum[];
    oidcEncryptAlgOptions: PropertyEnum[];
    oidcSubjectTypes: PropertyEnum[];
    canonicalizationModes: String[];
}

export interface PropertyEnum {
    display: String;
    value: String;
}
