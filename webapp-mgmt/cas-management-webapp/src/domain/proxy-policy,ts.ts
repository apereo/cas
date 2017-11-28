export abstract class RegisteredServiceProxyPolicy {

}

export class RegexMatchingRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  static cName =  'org.apereo.cas.services.RegexMatchingRegisteredServiceProxyPolicy';

  pattern: String;

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === RegexMatchingRegisteredServiceProxyPolicy.cName;
  }

  constructor() {
    super();
    this['@class'] = RegexMatchingRegisteredServiceProxyPolicy.cName;
  }
}

export class RefuseRegisteredServiceProxyPolicy extends RegisteredServiceProxyPolicy {
  static cName = 'org.apereo.cas.services.RefuseRegisteredServiceProxyPolicy';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === RefuseRegisteredServiceProxyPolicy.cName;
  }

  constructor() {
    super();
    this['@class'] = RefuseRegisteredServiceProxyPolicy.cName;
  }
}
