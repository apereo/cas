/**
 * Created by tschmidt on 2/13/17.
 */
import {Http} from '@angular/http'
import {Injectable} from "@angular/core";
import ServiceData from "../../domain/service-data";
import {Service} from "../service";

@Injectable()
export class DomainService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getDomains(): Promise<String[]> {
    return this.get<String[]>("domains");
  }

  getServiceList(domain: String): Promise<ServiceData[]> {
    return this.get<ServiceData[]>("services?domain="+domain);
  }

 }
