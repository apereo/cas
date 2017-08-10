
import {RegexRegisteredService, RegisteredService} from "./registered-service";

export class SamlRegisteredService extends RegexRegisteredService {
  metadataLocation: String;
  metadataMaxValidity: number;
  requiredAuthenticationContextClass: String;
  metadataCriteriaDirection: String;
  metadataCriteriaPattern: String;
  requiredNamedIdFormat: String;
  metadataSignatureLocation: String;
  serviceProviderNameIdQualifier: String;
  nameIdQualifier: String;
  metadataExpirationDuration: String;
  signAssertions: boolean;
  signResponses: boolean;
  encryptAssertions: boolean;
  metadataCriteriaRoles: String;
  metadataCriteriaRemoveEmptyEntitesDescriptors: boolean;
  metadataCriteriaRemoveRolelessEntityDescriptors: boolean;
  attributeNameFormats: Map<String, String>;

  static cName = "org.apereo.cas.support.saml.services.SamlRegisteredService";

  constructor(service?: RegisteredService) {
    super(service);
    this["@class"] = SamlRegisteredService.cName;
  }

  static instanceOf(service: RegisteredService): boolean {
    return SamlRegisteredService.cName == service["@class"];
  }


}
