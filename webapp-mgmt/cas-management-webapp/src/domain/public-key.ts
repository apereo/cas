export abstract class RegisteredServicePublicKey {
  location: String;
  algorithm: String;
  publicKeyFactoryBeanClass: any;
}

export class RegisteredServicePublicKeyImpl extends RegisteredServicePublicKey {
  static cName = 'org.apereo.cas.services.RegisteredServicePublicKeyImpl';

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === RegisteredServicePublicKeyImpl.cName;
  }

  constructor() {
    super();
    this.algorithm = 'RSA';
    this['@class'] = RegisteredServicePublicKeyImpl.cName;
  }
}
