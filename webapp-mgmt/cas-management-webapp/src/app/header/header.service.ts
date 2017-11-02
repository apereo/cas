
import {Service} from '../service';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class HeaderService extends Service {

  constructor(http: HttpClient) {
    super(http);
  }

  getMangerType(): Promise<String> {
    return this.getText('managerType');
  }
}
