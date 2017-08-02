export abstract class RegisteredServicePublicKey {
  location: String;
  algorithm: String;
  publicKeyFactoryBeanClass: any;
}

export class RegisteredServicePublicKeyImpl extends RegisteredServicePublicKey {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.RegisteredServicePublicKeyImpl";
  }
}
