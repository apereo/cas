
import {RegisteredServiceAccessStrategy} from "./access-strategy";
import {RegisteredServiceMultifactorPolicy} from "./multifactor";
import {RegisteredServiceProxyPolicy} from "./proxy-policy,ts";
import {RegisteredServiceUsernameAttributeProvider} from "./attribute-provider";
import {RegisteredServiceAttributeReleasePolicy} from "./attribute-release";
import {RegisteredServicePublicKey} from "./public-key";
import {DefaultRegisteredServiceProperty} from "./property";

export abstract class RegisteredService {
  serviceId: String;
  name: String;
  theme: String;
  informationUrl: String;
  privacyUrl: String;
  id: number;
  description: String;
  proxyPolicy: RegisteredServiceProxyPolicy;
  evaluationOrder: number;
  usernameAttributeProvider: RegisteredServiceUsernameAttributeProvider;
  requiredHandlers: String[] = [];
  attributeReleasePolicy: RegisteredServiceAttributeReleasePolicy;
  multifactorPolicy: RegisteredServiceMultifactorPolicy;
  logo: String;
  logoutUrl: String;
  logoutType: String;
  accessStrategy: RegisteredServiceAccessStrategy;
  publicKey: RegisteredServicePublicKey;
  properties: Map<String, DefaultRegisteredServiceProperty>;
}

export abstract class AbstractRegisteredService extends RegisteredService {

}

export class RegexRegisteredService extends AbstractRegisteredService {
  constructor() {
    super();
    this["@class"] = "org.apereo.cas.services.RegexRegisteredService";
  }
}
