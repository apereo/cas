
import {RegexRegisteredService, RegisteredService} from "./registered-service";

export class SamlRegisteredService extends RegexRegisteredService {
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


  static cName = "org.apereo.cas.support.saml.services.SamlRegisteredService";

  constructor(service?: RegisteredService) {
    super(service);
    this.metadataExpirationDuration = "PT60M";
    this.metadataCriteriaRoles = "SPSSODescriptor";
    this.signResponses = true;
    this.signingCredentialType = "BASIC_X509";
    this.metadataCriteriaRemoveEmptyEntitiesDescriptors = true;
    this.metadataCriteriaRemoveRolelessEntityDescriptors = true;
    this.skipGeneratingSubjectConfirmationNotBefore = true;
    this["@class"] = SamlRegisteredService.cName;
  }

  static instanceOf(service: RegisteredService): boolean {
    return SamlRegisteredService.cName == service["@class"];
  }


}
