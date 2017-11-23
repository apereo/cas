export abstract class RegisteredServiceMultifactorPolicy {
  multifactorAuthenticationProviders: String[];
  failureMode: String;
  principalAttributeNameTrigger: String;
  principalAttributeValueToMatch: String;
  bypassEnabled: boolean;
}

export class DefaultRegisteredServiceMultifactorPolicy extends RegisteredServiceMultifactorPolicy {
  static cName = 'org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceMultifactorPolicy.cName;
  }

  constructor() {
    super();
    this.failureMode = 'NOT_SET';
    this['@class'] = DefaultRegisteredServiceMultifactorPolicy.cName;
  }
}
