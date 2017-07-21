import ServiceProxyPolicy from "./service-proxy-policy";
/**
 * Created by tschmidt on 2/14/17.
 */
export class Form {
  formData: FormData = new FormData();
  serviceData: Data = new Data();
  status: number;
}

export class FormData {
  availableAttributes: String[] = [];
}

export class Data {
  assignedId: String;
  serviceId: String;
  name: String;
  description: String;
  logoUrl: String;
  theme: String;
  informationUrl: String;
  privacyUrl: String;
  evalOrder: number;
  requiredHandlers: String[] = [];
  reqHandlersStr: String[] = [];
  logoutUrl: String;
  multiAuth: MultiAuth = new MultiAuth();
  supportAccess: SupportAccess = new SupportAccess();
  type: String;
  oidc: OidcType = new OidcType();
  oauth: OAuthType = new OAuthType();
  saml: SamlType = new SamlType();
  logoutType: String;
  userAttrProvider: UsernameAttributeProvider = new UsernameAttributeProvider();
  publicKey: PublicKey = new PublicKey();
  proxyPolicy: ServiceProxyPolicy = new ServiceProxyPolicy();
  attrRelease: AttributeRelease = new AttributeRelease();
  properties: PropertyBean[] = [];
  wsfed: WSFed = new WSFed();
}

export class Contact {
  name: String;
  phone: String;
}

export class MultiAuth {
  failureMode: String;
  providers: String;
  bypassEnabled: boolean;
  principalAttr: PrincipalAttribute = new PrincipalAttribute();
}

export class PrincipalAttribute {
  valueMatch: String;
  nameTrigger: String;
}

export class OidcType {
  signToken: boolean;
  jwks: String;
  implicit: boolean;
  encrypt: boolean;
  encryptAlg: String;
  encryptEnc: String;
  dynamicDate: String;
  dynamic: boolean;
  scopes: String = "";
  type: String;
}

export class SamlType {
  signResp: boolean;
  signAssert: boolean;
  encAssert: boolean;
  authCtxCls: String;
  mdLoc: String;
  mdMaxVal: number;
  mdSigLoc: String;
  removeEmptyEntities: boolean;
  removeRoleless: boolean;
  mdPattern: String;
  dir: String;
  roles: String[];
}

export class SupportAccess {
  startingTime: String;
  endingTime: String;
  casEnabled: String;
  ssoEnabled: String;
  requireAll: String;
  unauthorizedRedirectUrl: String;
  type: String;
  groupField: String;
  codes: String;
  url: String;
  requiredAttr: Map<String, Array<String>> = new Map<String, Array<String>>();
  requiredAttrStr: Map<String,Array<String>> = new Map<String, Array<String>>();
  rejectedAttr: PropertyBean[] = [];
  caseInsensitive: boolean
}

export class PropertyBean {

  constructor(name: String, value: String) {
    this.name = name;
    this.value = value;
  }

  name: String;
  value: String;
}

export class OAuthType {
  clientSecret: String;
  clientId: String;
  bypass: boolean;
  refreshToken: boolean;
  jsonFormat: boolean;
}

export class UsernameAttributeProvider {
  value: String;
  type: String;
  valueAnon: String;
  valueAttr: String;
}

export class PublicKey {
  location: String;
  algorithm: String = "RSA";
}

export class AttributeRelease {
  attrFilter: String;
  cachedTimeUnit: String;
  cachedExpiration: number;
  attrPolicy: AttributeReleasePolicy = new AttributeReleasePolicy();
  attrOption: String;
  mergingStrategy: String;
  releasePassword: boolean;
  releaseTicket: boolean;
  excludeDefault: boolean;
}

export class AttributeReleasePolicy {
  type: String;
  attributes: any;
  scriptFile: String;
  mapped: Map<String,String> = new Map<String,String>();
  allowed;
  value;
  wsfed_only;
}

export class WSFed {
  realm: String;
  appliesTo: String;
}
