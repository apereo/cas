export abstract class RegisteredServiceMultifactorPolicy {
  multifactorAuthenticationProviders: String[];
  failureMode: String;
  principalAttributeNameTrigger: String;
  principalAttributeValueToMatch: String;
  bypassEnabled: boolean;
}

export class DefaultRegisteredServiceMultifactorPolicy extends RegisteredServiceMultifactorPolicy {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.DefaultRegisteredServiceMultifactorPolicy";
  }
}
