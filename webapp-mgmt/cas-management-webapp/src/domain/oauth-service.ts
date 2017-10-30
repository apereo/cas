import {RegexRegisteredService, RegisteredService} from './registered-service';

export class OAuthRegisteredService extends RegexRegisteredService {
  static cName = 'org.apereo.cas.support.oauth.services.OAuthRegisteredService';

  clientSecret: String;
  clientId: String;
  bypassApprovalPrompt: boolean;
  generateRefreshToken: boolean;
  jsonFormat: boolean;
  supportedGrantTypes: String[];
  supportedResponseTypes: String[];

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === OAuthRegisteredService.cName;
  }

  constructor(service?: RegisteredService) {
    super(service);
    this['@class'] = OAuthRegisteredService.cName;
    const s: OAuthRegisteredService = service as OAuthRegisteredService;
    this.clientSecret = s && s.clientSecret;
    this.clientId = s && s.clientId;
    this.bypassApprovalPrompt = s && s.bypassApprovalPrompt;
    this.generateRefreshToken = s && s.generateRefreshToken;
    this.jsonFormat = s && s.jsonFormat;
    this.supportedGrantTypes = s && s.supportedGrantTypes;
    this.supportedResponseTypes = s && s.supportedResponseTypes;
  }
}

export class OidcRegisteredService extends OAuthRegisteredService {
  static cName = 'org.apereo.cas.services.OidcRegisteredService';

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

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === OidcRegisteredService.cName;
  }

  constructor(service?: RegisteredService) {
    super(service);
    this.jsonFormat = true;
    this.signIdToken = true;
    this.subjectType = 'PUBLIC';
    this['@class'] = OidcRegisteredService.cName;
  }
}
