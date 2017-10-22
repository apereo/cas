/**
 * Created by tsschmi on 2/28/17.
 */

import {Injectable} from "@angular/core";
import {Service} from "../service";
import {Http} from "@angular/http";

@Injectable()
export class ControlsService extends Service {

  constructor (protected http: Http) {
    super(http);
  }


}
