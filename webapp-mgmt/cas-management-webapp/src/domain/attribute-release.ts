import {DefaultPrincipalAttributesRepository, PrincipalAttributesRepository} from "./attribute-repo";

export abstract class RegisteredServiceAttributeReleasePolicy {
  attributeFilter: RegisteredServiceAttributeFilter;
  principalAttributesRepository: PrincipalAttributesRepository;
  authorizedToReleaseCredentialPassword: boolean;
  authorizedToReleaseProxyGrantingTicket: boolean;
  excludeDefaultAttributes: boolean;
  principalIdAttribute: String;

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    this.attributeFilter = policy && policy.attributeFilter;
    this.principalAttributesRepository = (policy && policy.principalAttributesRepository) || new DefaultPrincipalAttributesRepository();
    this.authorizedToReleaseCredentialPassword = policy && policy.authorizedToReleaseCredentialPassword;
    this.authorizedToReleaseProxyGrantingTicket = policy && policy.authorizedToReleaseProxyGrantingTicket;
    this.excludeDefaultAttributes = policy && policy.excludeDefaultAttributes;
    this.principalIdAttribute = policy && policy.principalIdAttribute;
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

export abstract class RegisteredServiceAttributeFilter {
  order: number;
  pattern: String

  constructor(filter?: RegisteredServiceAttributeFilter) {
    this.pattern = (filter && filter.pattern) || "";
  }
}

export class RegisteredServiceRegexAttributeFilter extends RegisteredServiceAttributeFilter {
  static cName = "org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter";

  constructor(filter?) {
    super(filter);
    this["@class"] = RegisteredServiceRegexAttributeFilter.cName;
  }

  static instanceOf(obj: any): boolean {
    return obj["@class"] === RegisteredServiceRegexAttributeFilter.cName;
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

