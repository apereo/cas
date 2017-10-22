
import {Service} from "../service";
import {Injectable} from "@angular/core";
import {Http} from "@angular/http";

@Injectable()
export class HeaderService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  getMangerType(): Promise<String> {
    return this.get("managerType");
  }
}