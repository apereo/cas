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
  proxyPolicy: any;
  attrRelease: any;
}

export class FormData {
  availableAttributes: String[] = [];
  customComponent: Map<String,Map<String,any>>;
}
