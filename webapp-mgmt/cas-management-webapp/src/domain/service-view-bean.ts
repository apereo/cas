/**
 * Created by tschmidt on 2/13/17.
 */
export class ServiceItem {
  evalOrder: number;
  assignedId: String;
  serviceId: String;
  name: String;
  description: String;
}

export class FormData {
  availableAttributes: String[] = [];
}

export class ServiceDetails {
  releaseCredential: String;
  releaseProxyTicket: String;
  attributePolicy: String;
  proxyPolicy: String;
  proxyPolicyValue: String;
  description: String;
  logoUrl: String;
}
