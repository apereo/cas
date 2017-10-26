export abstract class RegisteredServiceExpirationPolicy {
  expirationDate: String;
  deleteWhenExpired: boolean;
  notifyWhenDeleted: boolean;

  constructor() {
  }
}

export class DefaultRegisteredServiceExpirationPolicy extends RegisteredServiceExpirationPolicy {
  static cName = 'org.apereo.cas.services.DefaultRegisteredServiceExpirationPolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === DefaultRegisteredServiceExpirationPolicy.cName;
  }

  constructor() {
    super();
    this['@class'] = DefaultRegisteredServiceExpirationPolicy.cName;
  }
}
