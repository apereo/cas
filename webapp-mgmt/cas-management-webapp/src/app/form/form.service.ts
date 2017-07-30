/**
 * Created by tschmidt on 2/14/17.
 */
import {Http, Headers} from '@angular/http'
import {Injectable} from "@angular/core";
import {ServiceEditBean, ServiceData} from "../../domain/service-edit-bean";

@Injectable()
export class FormService {

  constructor(private http: Http) {}

  getService(id: string): Promise<ServiceEditBean> {
    return this.http.get("getService?id="+id)
      .toPromise()
      .then(resp => resp.json())
      .catch(this.handleError)
  }

  saveService(serviceData: ServiceData): Promise<String> {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });

    return this.http
      .post("saveService", JSON.stringify(serviceData), {headers: headers})
      .toPromise()
      .then(resp => resp.text())
      .catch(this.handleError)
  }

  handleError(e: any) : Promise<any> {
    console.log("An error occurred : "+e);
    return Promise.reject(e.message || e);
  }

}
