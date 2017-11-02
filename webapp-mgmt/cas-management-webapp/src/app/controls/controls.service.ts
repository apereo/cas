/**
 * Created by tsschmi on 2/28/17.
 */

import {Injectable} from '@angular/core';
import {Service} from '../service';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class ControlsService extends Service {

  constructor(http: HttpClient) {
    super(http);
  }
}
