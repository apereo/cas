/**
 * Created by tschmidt on 2/14/17.
 */
import {Http, Headers} from '@angular/http'
import {Injectable} from "@angular/core";
import ServiceView from "../../domain/service-view";
import {Form, Data, Contact} from "../../domain/form";

@Injectable()
export class FormService {

  constructor(private http: Http) {}

  getService(id: string): Promise<Form> {
    return this.http.get("getService?id="+id)
      .toPromise()
      .then(resp => resp.json())
      .catch(this.handleError)
  }

  saveService(serviceData: Data): Promise<String> {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });

    return this.http
      .post("saveService.html", JSON.stringify(serviceData), {headers: headers})
      .toPromise()
      .then(resp => resp.text())
      .catch(this.handleError)
  }

  handleError(e: any) : Promise<any> {
    console.log("An error occurred : "+e);
    return Promise.reject(e.message || e);
  }

}
