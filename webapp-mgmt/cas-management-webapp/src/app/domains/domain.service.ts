/**
 * Created by tschmidt on 2/13/17.
 */
import {HttpClient} from '@angular/common/http'
import {Injectable} from '@angular/core';
import {Service} from '../service';

@Injectable()
export class DomainService extends Service {

  constructor(http: HttpClient) {
    super(http);
  }

  getDomains(): Promise<String[]> {
    return this.get<String[]>('domainList');
  }

 }
