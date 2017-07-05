/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from "@angular/core";
import {Resolve, Router, ActivatedRouteSnapshot} from "@angular/router";
import ServiceView from "../../domain/service-view";
import {ServiceViewService} from "./service.service";

@Injectable()
export class ServicesResolve implements Resolve<ServiceView[]> {

  constructor(private service: ServiceViewService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<ServiceView[]> {

    return this.service.getServices().then(resp => resp ? resp : null);
  }
}
