
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

  constructor(service?: RegisteredService) {
    this.serviceId = service && service.serviceId;
    this.name = service && service.name;
    this.theme = service && service.theme;
    this.informationUrl = service && service.informationUrl;
    this.privacyUrl = service && service.privacyUrl;
    this.id = service && service.id;
    this.description = service && service.description;
    this.proxyPolicy = service && service.proxyPolicy;
    this.evaluationOrder = service && service.evaluationOrder;
    this.usernameAttributeProvider = service && service.usernameAttributeProvider;
    this.requiredHandlers = service && service.requiredHandlers;
    this.attributeReleasePolicy = service && service.attributeReleasePolicy;
    this.multifactorPolicy = service && service.multifactorPolicy;
    this.logo = service && service.logo;
    this.logoutUrl = service && service.logoutUrl;
    this.logoutType = service && service.logoutType;
    this.accessStrategy = service && service.accessStrategy;
    this.publicKey = service && service.publicKey;
    this.properties = service && service.properties;
  }
}

export abstract class AbstractRegisteredService extends RegisteredService {
  constructor(service?: RegisteredService) {
    super(service);
  }
}

export class RegexRegisteredService extends AbstractRegisteredService {
  constructor(service?: RegisteredService) {
    super(service);
    this["@class"] = "org.apereo.cas.services.RegexRegisteredService";
  }
}
