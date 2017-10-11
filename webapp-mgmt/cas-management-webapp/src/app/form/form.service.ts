/**
 * Created by tschmidt on 2/14/17.
 */
import {Http, Headers} from '@angular/http'
import {Injectable} from "@angular/core";
import {AbstractRegisteredService} from "../../domain/registered-service";
import {FormData} from "../../domain/service-view-bean";

@Injectable()
export class FormService {

  constructor(private http: Http) {}

  getService(id: string): Promise<AbstractRegisteredService> {
    return this.http.get("getService?id="+id)
      .toPromise()
      .then(resp => {
        let as: AbstractRegisteredService = resp.json() as AbstractRegisteredService;
        return as;
      })
      .catch(this.handleError)
  }

  saveService(service: AbstractRegisteredService): Promise<number> {
    let headers = new Headers({
      'Content-Type': 'application/json'
    });

    return this.http
      .post("saveService", JSON.stringify(service), {headers: headers})
      .toPromise()
      .then(resp => resp.text())
      .catch(this.handleError)
  }


  formData(): Promise<FormData> {
    return this.http.get("formData")
      .toPromise()
      .then(resp => resp.json())
      .catch(this.handleError);
  }

  handleError(e: any) : Promise<any> {
    console.log("An error occurred : "+e);
    return Promise.reject(e.message || e);
  }

}
