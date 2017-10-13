import {DefaultPrincipalAttributesRepository, PrincipalAttributesRepository} from "./attribute-repo";
import {RegisteredServiceAttributeFilter} from "./attribute-filter";

export abstract class RegisteredServiceAttributeReleasePolicy {
  attributeFilter: RegisteredServiceAttributeFilter;
  principalAttributesRepository: PrincipalAttributesRepository;
  authorizedToReleaseCredentialPassword: boolean;
  authorizedToReleaseProxyGrantingTicket: boolean;
  excludeDefaultAttributes: boolean;
  authorizedToReleaseAuthenticationAttributes: boolean;
  principalIdAttribute: String;
  consentPolicy: RegisteredServiceConsentPolicy;

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    this.attributeFilter = policy && policy.attributeFilter;
    this.principalAttributesRepository = (policy && policy.principalAttributesRepository) || new DefaultPrincipalAttributesRepository();
    this.authorizedToReleaseCredentialPassword = policy && policy.authorizedToReleaseCredentialPassword;
    this.authorizedToReleaseProxyGrantingTicket = policy && policy.authorizedToReleaseProxyGrantingTicket;
    this.excludeDefaultAttributes = policy && policy.excludeDefaultAttributes;
    this.principalIdAttribute = policy && policy.principalIdAttribute;
    this.authorizedToReleaseAuthenticationAttributes = policy && policy.authorizedToReleaseAuthenticationAttributes || true;
    this.consentPolicy = policy && policy.consentPolicy || new DefaultRegisteredServiceConsentPolicy();
  }
}

export abstract class AbstractRegisteredServiceAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
  }
}

export class ReturnAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

  static cName = "org.apereo.cas.services.ReturnAllAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = ReturnAllAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === this.cName;
  }
}

export class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {

  static cName = "org.apereo.cas.services.DenyAllAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = DenyAllAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === DenyAllAttributeReleasePolicy.cName;
  }
}

export class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  allowedAttributes: Map<String,any>;

  static cName = "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = ReturnMappedAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === ReturnMappedAttributeReleasePolicy.cName;
  }
}

export class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  allowedAttributes: String[];

  static cName = "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = ReturnAllowedAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === ReturnAllowedAttributeReleasePolicy.cName;
  }
}

export class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  scriptFile: String;

  static cName = "org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = ScriptedRegisteredServiceAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === ScriptedRegisteredServiceAttributeReleasePolicy.cName;
  }
}

export class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  groovyScript: String;

  static cName =  "org.apereo.cas.services.GroovyScriptAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = GroovyScriptAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === GroovyScriptAttributeReleasePolicy.cName;
  }
}

export class WsFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  allowedAttributes: Map<String,String>

  static cName = "org.apereo.cas.ws.idp.services.WsFederationClaimsReleasePolicy";

  constructor(policy?: AbstractRegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = WsFederationClaimsReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === WsFederationClaimsReleasePolicy.cName;
  }
}

export class InCommonRSAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
  static cName = "org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = InCommonRSAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === InCommonRSAttributeReleasePolicy.cName;
  }
}

export class PatternMatchingEntityIdAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
  static cName = "org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = PatternMatchingEntityIdAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === PatternMatchingEntityIdAttributeReleasePolicy.cName;
  }
}

export class MetadataEntityAttributesAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
  entityAttribute: String;
  entityAttributeFormat: String;
  entityAttributeValues: String[];

  static cName = "org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy";

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = MetadataEntityAttributesAttributeReleasePolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj && obj["@class"] === MetadataEntityAttributesAttributeReleasePolicy.cName;
  }
}

export class RegisteredServiceConsentPolicy {
  enabled: boolean;
  excludedAttributes: String[];
  includeOnlyAttributes: String[];

  static cName = "org.apereo.cas.services.RegisteredServiceConsentPolicy";

  constructor() {
    this.enabled = true;
    this["@class"] = RegisteredServiceConsentPolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === RegisteredServiceConsentPolicy.cName
  }
}

export class DefaultRegisteredServiceConsentPolicy extends RegisteredServiceConsentPolicy {
  static cName = "org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy";

  constructor() {
    super();
    this["@class"] = DefaultRegisteredServiceConsentPolicy.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj && obj["@class"] === DefaultRegisteredServiceConsentPolicy.cName;
  }
}
