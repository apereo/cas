import {RegexRegisteredService, RegisteredService} from './registered-service';

export class SamlRegisteredService extends RegexRegisteredService {
  static cName = 'org.apereo.cas.support.saml.services.SamlRegisteredService';

  metadataLocation: String;
  metadataMaxValidity: number;
  requiredAuthenticationContextClass: String;
  metadataCriteriaDirection: String;
  metadataCriteriaPattern: String;
  requiredNameIdFormat: String;
  metadataSignatureLocation: String;
  serviceProviderNameIdQualifier: String;
  nameIdQualifier: String;
  metadataExpirationDuration: String;
  signAssertions: boolean;
  signResponses: boolean;
  signingCredentialType: String;
  encryptAssertions: boolean;
  metadataCriteriaRoles: String;
  metadataCriteriaRemoveEmptyEntitiesDescriptors: boolean;
  metadataCriteriaRemoveRolelessEntityDescriptors: boolean;
  attributeNameFormats: Map<String, String>;
  skipGeneratingAssertionNameId: boolean;
  skipGeneratingSubjectConfirmationInResponseTo: boolean;
  skipGeneratingSubjectConfirmationNotOnOrAfter: boolean;
  skipGeneratingSubjectConfirmationRecipient: boolean;
  skipGeneratingSubjectConfirmationNotBefore: boolean;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === SamlRegisteredService.cName;
  }

  constructor(service?: RegisteredService) {
    super(service);
    this.metadataExpirationDuration = 'PT60M';
    this.metadataCriteriaRoles = 'SPSSODescriptor';
    this.signResponses = true;
    this.signingCredentialType = 'BASIC';
    this.metadataCriteriaRemoveEmptyEntitiesDescriptors = true;
    this.metadataCriteriaRemoveRolelessEntityDescriptors = true;
    this.skipGeneratingSubjectConfirmationNotBefore = true;
    this['@class'] = SamlRegisteredService.cName;
  }
}
