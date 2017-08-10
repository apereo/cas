/**
 * Created by tschmidt on 2/13/17.
 */
export class ServiceViewBean {
  evalOrder: number;
  assignedId: String;
  sasCASEnabled: boolean;
  serviceId: String;
  name: String;
  description: String;
  logoUrl: String;
  proxyPolicy: RegisteredServiceProxyPolicyBean;
  attrRelease: RegisteredServiceAttributeReleasePolicyBean;
}

export class FormData {
  availableAttributes: String[] = [];
  customComponent: Map<String,Map<String,any>>;
}

export class RegisteredServiceProxyPolicyBean {
  type: String;
  value: String;
}

export class RegisteredServiceAttributeReleasePolicyBean {
  releasePassword: boolean;
  releaseTicket: boolean;
  excludeDefault: boolean;
  attrPolicy: String;
}
