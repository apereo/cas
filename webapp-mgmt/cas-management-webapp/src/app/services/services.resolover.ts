/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from "@angular/core";
import {Resolve, Router, ActivatedRouteSnapshot} from "@angular/router";
import {ServiceViewBean} from "../../domain/service-view-bean";
import {ServiceViewService} from "./service.service";

@Injectable()
export class ServicesResolve implements Resolve<ServiceViewBean[]> {

  constructor(private service: ServiceViewService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<ServiceViewBean[]> {

    return this.service.getServices().then(resp => resp ? resp : null);
  }
}
