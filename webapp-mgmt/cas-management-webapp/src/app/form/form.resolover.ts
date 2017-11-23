/**
 * Created by tschmidt on 2/13/17.
 */

import {Injectable} from '@angular/core';
import {Resolve, Router, ActivatedRouteSnapshot} from '@angular/router';
import {FormService} from './form.service';
import {AbstractRegisteredService, RegexRegisteredService} from '../../domain/registered-service';

@Injectable()
export class FormResolve implements Resolve<AbstractRegisteredService> {

  constructor(private service: FormService, private router: Router) {}

  resolve(route: ActivatedRouteSnapshot): Promise<AbstractRegisteredService> {
    const param: string = route.params['id'];

    if (!param || param === '-1') {
      return new Promise((resolve, reject) => resolve(new RegexRegisteredService()));
    } else {
      return this.service.getService(param).then(resp => {
        if (resp) {
          if (route.data.duplicate) {
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
