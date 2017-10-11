/**
 * Created by tsschmi on 4/25/17.
 */

import {Http, Headers, ResponseContentType} from '@angular/http';

export abstract class Service {

  constructor(protected http: Http) {

  }

  headers(): Headers {
    return new Headers({
      'Content-Type': 'application/json'
    });
  }

  post<T>(url: string ,data: any): Promise<T> {
    return this.http.post(url,JSON.stringify(data), { headers: this.headers()})
      .toPromise()
      .then(resp => resp.text().startsWith("{") || resp.text().startsWith("[") ? resp.json() : resp.text())
      .catch(this.handleError);
  }

  get<T>(url: string): Promise<T> {
    return this.http.get(url)
      .toPromise()
      .then(resp => resp.text().startsWith("{") || resp.text().startsWith("[") ? resp.json() : resp.text())
      .catch(this.handleError);
  }

  handleError(e: any): Promise<any> {
    console.log("An error Occurred: "+e);
    return Promise.reject(e.message || e);
  }
}

