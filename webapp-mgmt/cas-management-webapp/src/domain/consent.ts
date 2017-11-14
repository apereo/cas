export class RegisteredServiceConsentPolicy {
  static cName = 'org.apereo.cas.services.RegisteredServiceConsentPolicy';

  enabled: boolean;
  excludedAttributes: String[];
  includeOnlyAttributes: String[];

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === RegisteredServiceConsentPolicy.cName
  }

  constructor() {
    this.enabled = true;
    this['@class'] = RegisteredServiceConsentPolicy.cName;
  }
}

export class DefaultRegisteredServiceConsentPolicy extends RegisteredServiceConsentPolicy {
  static cName = 'org.apereo.cas.services.consent.DefaultRegisteredServiceConsentPolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceConsentPolicy.cName;
  }

  constructor() {
    super();
    this['@class'] = DefaultRegisteredServiceConsentPolicy.cName;
  }
}
