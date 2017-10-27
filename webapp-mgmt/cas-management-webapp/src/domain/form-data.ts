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
  samlNameIds: String[];
  samlCredentialTypes: String[];
  wsFederationClaims: String[];
  mfaProviders: PropertyEnum[];
  mfaFailureModes: String[];
  oidcScopes: PropertyEnum[];
  oidcEncodingAlgOptions: String[];
  oidcEncryptAlgOptions: String[];
  oidcSubjectTypes: PropertyEnum[];
  canonicalizationModes: String[];
}

export interface PropertyEnum {
  display: String;
  value: String;
}