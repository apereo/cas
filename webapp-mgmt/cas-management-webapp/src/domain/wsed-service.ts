import {RegexRegisteredService, RegisteredService} from "./registered-service";

export class WSFederationRegisterdService extends RegexRegisteredService {
  realm: String;
  protocol: String;
  tokenType: String;
  wsdlLocation: String;
  namespace: String;
  addressingNamespace: String;
  policyNamespace: String;
  wsdlService: String;
  wsdlEndpoint: String;
  appliesTo: String;

  static readonly cName = "org.apereo.cas.ws.idp.services.WSFederationRegisteredService";

  constructor(service?: RegisteredService) {
    super(service);
    this["@class"] = WSFederationRegisterdService.cName;
  }

  static instanceOf(service: RegisteredService): boolean {
    return service["@class"] === WSFederationRegisterdService.cName;
  }
}
