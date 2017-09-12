/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from "@angular/core";
import {ServiceViewBean} from "../../domain/service-view-bean";
import {Service} from "../service";
import {Http} from "@angular/http";

@Injectable()
export class ServiceViewService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getServices(domain: String): Promise<ServiceViewBean[]> {
    return this.get<ServiceViewBean[]>("getServices?domain="+domain);
  }

  delete(id: number): Promise<String> {
    return this.get<String>("deleteRegisteredService?id="+id);
  }

  updateOrder(a: ServiceViewBean, b: ServiceViewBean): Promise<String> {
    return this.post<String>("updateOrder",[a,b]);
  }

}
