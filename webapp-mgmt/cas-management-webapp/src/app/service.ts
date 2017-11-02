/**
 * Created by tsschmi on 4/25/17.
 */

import { Headers, ResponseContentType} from '@angular/http';
import {HttpClient} from '@angular/common/http';
import {textDef} from '@angular/core/src/view';

export abstract class Service {

  constructor(protected http: HttpClient) {

  }

  headers(): Headers {
    return new Headers({
      'Content-Type': 'application/json'
    });
  }

  post<T>(url: string , data: any): Promise<T> {
    return this.http.post<T>(url, JSON.stringify(data))
      .toPromise()
      .then(resp => resp)
      .catch(this.handleError);
  }

  get<T>(url: string): Promise<T> {
    return this.http.get(url)
      .toPromise()
      .then(resp => resp)
      .catch(this.handleError);
  }

  getText(url: string): Promise<String> {
    return this.http.get(url, {responseType: 'text'})
      .toPromise()
      .then(resp => resp)
      .catch(this.handleError);
  }

  handleError(e: any): Promise<any> {
    console.log('An error Occurred: ' + e);
    return Promise.reject(e.message || e);
  }
}

