/**
 * Created by tschmidt on 2/13/17.
 */
import {Injectable} from "@angular/core";
import ServiceView from "../../domain/service-view";
import {Service} from "../service";
import {Http} from "@angular/http";

@Injectable()
export class ServiceViewService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getServices(): Promise<ServiceView[]> {
    return this.get<ServiceView[]>("getServices").then(resp => resp['services']);
  }

  delete(id: number): Promise<String> {
    return this.get<String>("deleteRegisteredService?id="+id);
  }

  updateOrder(a: ServiceView, b: ServiceView): Promise<String> {
    return this.post<String>("updateOrder",[a,b]);
  }

}
