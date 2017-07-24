/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from "@angular/core";
import {Resolve, Router, ActivatedRouteSnapshot} from "@angular/router";
import {ServiceEditBean} from "../../domain/service-edit-bean";
import {FormService} from "./form.service";

@Injectable()
export class FormResolve implements Resolve<ServiceEditBean> {

  constructor(private service: FormService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<ServiceEditBean> {
    let param: string = route.params['id'];
    let dup: boolean = route.params['duplicate'];

    if(!param || param === '-1') {
      return new Promise((resolve,reject) => resolve(null));
    } else {
      return this.service.getService(param).then(resp => {
        if (resp) {
          if (dup) {
            resp.serviceData.assignedId = '-1';
          }
          return resp;
        } else {
          return null;
        }
      });
    }
  }
}
