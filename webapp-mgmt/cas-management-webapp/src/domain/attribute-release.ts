import {PrincipalAttributesRepository} from "./attribute-repo";

export abstract class RegisteredServiceAttributeReleasePolicy {
  attributeFilter: RegisteredServiceAttributeFilter;
  principalAttributesRepository: PrincipalAttributesRepository;
  authorizedToReleaseCredentialPassword: boolean;
  authorizedToReleaseProxyGrantingTicket: boolean;
  excludeDefaultAttributes: boolean;
  principalIdAttribute: String;
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    this.attributeFilter = policy && policy.attributeFilter;
    this.principalAttributesRepository = policy && policy.principalAttributesRepository;
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
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.ReturnAllAttributeReleasePolicy";
  }
}

export class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.DenyAllAttributeReleasePolicy";
  }
}

export class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  allowedAttributes: Map<String,any>;
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.ReturnMappedAttributeReleasePolicy";
  }
}

export class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  allowedAttributes: String[];
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy";
  }
}

export class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  scriptFile: String;
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy";
  }
}

export class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  groovyScript: String;
  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this["@class"] = "org.apereo.cas.services.GroovyScriptAttributeReleasePolicy";
  }
}

export abstract class RegisteredServiceAttributeFilter {
  order: number;
}

export class RegisteredServiceRegexAttributeFilter extends RegisteredServiceAttributeFilter {
  pattern: String;
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.support.RegisteredServiceRegexAttributeFilter";
  }
}

export class RegisteredServiceScriptedAttributeFilter extends RegisteredServiceAttributeFilter {
  script: String;
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.support.RegisteredServiceScriptedAttributeFilter";
  }
}

export class RegisteredServiceMappedRegexAttributeFilter extends RegisteredServiceAttributeFilter {
  patterns: Map<String, String>;
  excludeUnmappedAttributes: boolean;
  completeMatch: boolean
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.support.RegisteredServiceMappedRegexAttributeFilter";
  }
}
