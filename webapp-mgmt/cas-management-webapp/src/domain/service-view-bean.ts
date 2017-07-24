import {ProxyPolicy, AttributeRelease} from "./service-edit-bean";
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
  proxyPolicy: ProxyPolicy;
  attrRelease: AttributeRelease;
}
