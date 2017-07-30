/**
 * Created by tschmidt on 2/14/17.
 */
export class ServiceEditBean {
  formData: FormData = new FormData();
  serviceData: ServiceData = new ServiceData();
  status: number;
}

export class FormData {
  availableAttributes: String[] = [];
  customComponent: Map<String, Map<String,any>>
}

export class ServiceData {
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
  proxyPolicy: ProxyPolicy = new ProxyPolicy();
  attrRelease: AttributeRelease = new AttributeRelease();
  customComponent: Map<String, Map<String,any>>
  properties: PropertyBean[] = [];
  wsfed: WSFed = new WSFed();
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

export class OAuthType {
  clientSecret: String;
  clientId: String;
  bypass: boolean;
  refreshToken: boolean;
  jsonFormat: boolean;
}

export class OidcType extends OAuthType {
  signToken: boolean;
  jwks: String;
  implicit: boolean;
  encrypt: boolean;
  encryptAlg: String;
  encryptEnc: String;
  dynamicDate: String;
  dynamic: boolean;
  scopes: String = "";
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
  casEnabled: boolean;
  ssoEnabled: boolean;
  requireAll: boolean;
  unauthorizedRedirectUrl: String;
  type: String;
  groupField: String;
  codes: String;
  url: String;
  requiredAttr: Map<String, Array<String>> = new Map<String, Array<String>>();
  requiredAttrStr: Map<String,Array<String>> = new Map<String, Array<String>>();
  rejectedAttr: PropertyBean[] = [];
  caseSensitive: boolean
}

export class PropertyBean {

  constructor(name: String, value: String) {
    this.name = name;
    this.value = value;
  }

  name: String;
  value: String;
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

export class ProxyPolicy {
  type: String;
  value: String;
}
