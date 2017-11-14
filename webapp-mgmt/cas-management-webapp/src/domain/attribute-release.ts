import {DefaultPrincipalAttributesRepository, PrincipalAttributesRepository} from './attribute-repo';
import {RegisteredServiceAttributeFilter} from './attribute-filter';
import {DefaultRegisteredServiceConsentPolicy, RegisteredServiceConsentPolicy} from './consent';

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
  static cName = 'org.apereo.cas.services.ReturnAllAttributeReleasePolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === this.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = ReturnAllAttributeReleasePolicy.cName;
  }
}

export class DenyAllAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.services.DenyAllAttributeReleasePolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DenyAllAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = DenyAllAttributeReleasePolicy.cName;
  }
}

export class ReturnMappedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.services.ReturnMappedAttributeReleasePolicy';

  allowedAttributes: Map<String, any>;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === ReturnMappedAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = ReturnMappedAttributeReleasePolicy.cName;
  }
}

export class ReturnAllowedAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.services.ReturnAllowedAttributeReleasePolicy';

  allowedAttributes: String[];

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === ReturnAllowedAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = ReturnAllowedAttributeReleasePolicy.cName;
  }
}

export class ScriptedRegisteredServiceAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.services.ScriptedRegisteredServiceAttributeReleasePolicy';

  scriptFile: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === ScriptedRegisteredServiceAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = ScriptedRegisteredServiceAttributeReleasePolicy.cName;
  }
}

export class ReturnRestfulAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.services.ReturnRestfulAttributeReleasePolicy';

  endpoint: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === ReturnRestfulAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = ReturnRestfulAttributeReleasePolicy.cName;
  }
}

export class GroovyScriptAttributeReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName =  'org.apereo.cas.services.GroovyScriptAttributeReleasePolicy';

  groovyScript: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === GroovyScriptAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = GroovyScriptAttributeReleasePolicy.cName;
  }
}

export class GroovySamlRegisteredServiceAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
  static cName =  'org.apereo.cas.support.saml.services.GroovySamlRegisteredServiceAttributeReleasePolicy';

  groovyScript: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === GroovySamlRegisteredServiceAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = GroovySamlRegisteredServiceAttributeReleasePolicy.cName;
  }
}

export class WsFederationClaimsReleasePolicy extends AbstractRegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.ws.idp.services.WsFederationClaimsReleasePolicy';

  allowedAttributes: Map<String, String>

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === WsFederationClaimsReleasePolicy.cName;
  }

  constructor(policy?: AbstractRegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = WsFederationClaimsReleasePolicy.cName;
  }
}

export class InCommonRSAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.support.saml.services.InCommonRSAttributeReleasePolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === InCommonRSAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = InCommonRSAttributeReleasePolicy.cName;
  }
}

export class PatternMatchingEntityIdAttributeReleasePolicy extends RegisteredServiceAttributeReleasePolicy {
  static cName = 'org.apereo.cas.support.saml.services.PatternMatchingEntityIdAttributeReleasePolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === PatternMatchingEntityIdAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = PatternMatchingEntityIdAttributeReleasePolicy.cName;
  }
}

export class MetadataEntityAttributesAttributeReleasePolicy extends ReturnAllowedAttributeReleasePolicy {
  static cName = 'org.apereo.cas.support.saml.services.MetadataEntityAttributesAttributeReleasePolicy';

  entityAttribute: String;
  entityAttributeFormat: String;
  entityAttributeValues: String[];

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === MetadataEntityAttributesAttributeReleasePolicy.cName;
  }

  constructor(policy?: RegisteredServiceAttributeReleasePolicy) {
    super(policy);
    this['@class'] = MetadataEntityAttributesAttributeReleasePolicy.cName;
  }
}
