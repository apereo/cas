/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from "@angular/core";
import {Resolve, Router, ActivatedRouteSnapshot} from "@angular/router";
import {FormService} from "./form.service";
import {AbstractRegisteredService} from "../../domain/registered-service";

@Injectable()
export class FormResolve implements Resolve<AbstractRegisteredService> {

  constructor(private service: FormService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<AbstractRegisteredService> {
    let param: string = route.params['id'];
    let dup: boolean = route.params['duplicate'];

    if(!param || param === '-1') {
      return new Promise((resolve,reject) => resolve(null));
    } else {
      return this.service.getService(param).then(resp => {
        if (resp) {
          if (dup) {
            resp.id = -1;
          }
          return resp;
        } else {
          return null;
        }
      });
    }
  }
}
