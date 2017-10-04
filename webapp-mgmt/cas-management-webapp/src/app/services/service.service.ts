/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from "@angular/core";
import {ServiceDetails, ServiceItem} from "../../domain/service-view-bean";
import {Service} from "../service";
import {Http} from "@angular/http";

@Injectable()
export class ServiceViewService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getServices(domain: String): Promise<ServiceItem[]> {
    return this.get<ServiceItem[]>("getServices?domain="+domain);
  }

  getDetails(id: String): Promise<ServiceDetails> {
    return this.get<ServiceDetails>("serviceDetails?id="+id);
  }

  delete(id: number): Promise<String> {
    return this.get<String>("deleteRegisteredService?id="+id);
  }

  updateOrder(a: ServiceItem, b: ServiceItem): Promise<String> {
    return this.post<String>("updateOrder",[a,b]);
  }

}
