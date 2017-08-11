
import {DefaultRegisteredServiceAccessStrategy, RegisteredServiceAccessStrategy} from "./access-strategy";
import {DefaultRegisteredServiceMultifactorPolicy, RegisteredServiceMultifactorPolicy} from "./multifactor";
import {RefuseRegisteredServiceProxyPolicy, RegisteredServiceProxyPolicy} from "./proxy-policy,ts";
import {
  DefaultRegisteredServiceUsernameProvider,
  RegisteredServiceUsernameAttributeProvider
} from "./attribute-provider";
import {
  RegisteredServiceAttributeReleasePolicy, ReturnAllAttributeReleasePolicy,
  ReturnAllowedAttributeReleasePolicy
} from "./attribute-release";
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
    this.id = (service && service.id) || -1;
    this.description = service && service.description;
    this.proxyPolicy = (service && service.proxyPolicy) || new RefuseRegisteredServiceProxyPolicy();
    this.evaluationOrder = (service && service.evaluationOrder) || -1;
    this.usernameAttributeProvider = (service && service.usernameAttributeProvider) || new DefaultRegisteredServiceUsernameProvider();
    this.requiredHandlers = service && service.requiredHandlers;
    this.attributeReleasePolicy = (service && service.attributeReleasePolicy) || new ReturnAllowedAttributeReleasePolicy();
    this.multifactorPolicy = (service && service.multifactorPolicy) || new DefaultRegisteredServiceMultifactorPolicy();
    this.logo = service && service.logo;
    this.logoutUrl = service && service.logoutUrl;
    this.logoutType = (service && service.logoutType) || "BACK_CHANNEL";
    this.accessStrategy = (service && service.accessStrategy) || new DefaultRegisteredServiceAccessStrategy();
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
  static cName = "org.apereo.cas.services.RegexRegisteredService";
  constructor(service?: RegisteredService) {
    super(service);
    this["@class"] = RegexRegisteredService.cName;
  }
  static instanceOf(obj: any): boolean {
    return obj && obj["@class"] === RegexRegisteredService.cName;
  }
}
