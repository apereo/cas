/**
 * Created by tschmidt on 2/13/17.
 */
import {RegisteredServiceProperty} from './property';

export class ServiceItem {
  evalOrder: number;
  assignedId: String;
  serviceId: String;
  name: String;
  description: String;
}

export class FormData {
  availableAttributes: String[] = [];
  registeredServiceProperties: RegisteredServiceProperty[];
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
  mfaProviders: any[];
  mfaFailureModes: String[];
  oidcScopes: any[];
  oidcEncodingAlgOptions: String[];
  oidcEncryptAlgOptions: String[];
  oidcSubjectTypes: any[];
  canonicalizationModes: String[];
}

