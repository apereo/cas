
import {RegexRegisteredService, RegisteredService} from "./registered-service";

export class OAuthRegisteredService extends RegexRegisteredService {
  clientSecret: String;
  clientId: String;
  bypassApprovalPrompt: boolean;
  generateRefreshToken: boolean;
  jsonFormat: boolean;
  supportedGrantTypes: String[];
  supportedResponseTypes: String[];

  static cName = "org.apereo.cas.support.oauth.services.OAuthRegisteredService";

  constructor(service?: RegisteredService) {
    super(service);
    this["@class"] = OAuthRegisteredService.cName;
    let s: OAuthRegisteredService = service as OAuthRegisteredService;
    this.clientSecret = s && s.clientSecret;
    this.clientId = s && s.clientId;
    this.bypassApprovalPrompt = s && s.bypassApprovalPrompt;
    this.generateRefreshToken = s && s.generateRefreshToken;
    this.jsonFormat = s && s.jsonFormat;
    this.supportedGrantTypes = s && s.supportedGrantTypes;
    this.supportedResponseTypes = s && s.supportedResponseTypes;
  }

  static instanceOf(service: RegisteredService): boolean {
    return OAuthRegisteredService.cName === service["@class"];
  }
}

export class OidcRegisteredService extends OAuthRegisteredService {
  jwks: String;
  signIdToken: boolean;
  encryptIdToken: boolean;
  idTokenEncryptionAlg: String;
  idTokenEncryptionEncoding: String;
  dynamicallyRegistered: boolean;
  implicit: boolean;
  dynamicRegistrationDateTime: String;
  scopes: String[];
  subjectType: String;
  sectorIdentifierUri: String;

  static cName = "org.apereo.cas.services.OidcRegisteredService";

  constructor(service?: RegisteredService) {
    super(service);
    this.jsonFormat = true;
    this.signIdToken = true;
    this.subjectType = "public";
    this["@class"] = OidcRegisteredService.cName;
  }

  static instanceOf(service: RegisteredService): boolean {
    return OidcRegisteredService.cName === service["@class"];
  }
}
