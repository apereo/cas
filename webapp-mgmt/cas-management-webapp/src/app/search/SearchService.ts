import {Injectable} from "@angular/core";
import {Service} from "../service";
import {Http} from "@angular/http";
import {ServiceItem} from "../../domain/service-view-bean";

@Injectable()
export class SearchService extends Service {

  constructor(protected http: Http) {
    super(http);
  }

  search(query: String): Promise<ServiceItem[]> {
    return this.get("search?query="+query);
  }
}
