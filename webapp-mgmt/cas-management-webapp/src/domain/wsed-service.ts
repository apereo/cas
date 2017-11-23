import {RegexRegisteredService, RegisteredService} from './registered-service';

export class WSFederationRegisterdService extends RegexRegisteredService {
  static readonly cName = 'org.apereo.cas.ws.idp.services.WSFederationRegisteredService';

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

  static instanceOf(obj: any): boolean {
    return obj && obj['@class'] === WSFederationRegisterdService.cName;
  }

  constructor(service?: RegisteredService) {
    super(service);
    this['@class'] = WSFederationRegisterdService.cName;
  }
}
