import ServiceProxyPolicy from "./service-proxy-policy";
import ServiceAttrRelease from "./service-attr-release";
/**
 * Created by tschmidt on 2/13/17.
 */
export default class ServiceView {
  evalOrder: number;
  assignedId: String;
  sasCASEnabled: boolean;
  serviceId: String;
  name: String;
  description: String;
  logoUrl: String;
  proxyPolicy: ServiceProxyPolicy;
  attrRelease: ServiceAttrRelease;
}
