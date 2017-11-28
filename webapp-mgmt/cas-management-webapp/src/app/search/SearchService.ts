import {Injectable} from '@angular/core';
import {Service} from '../service';
import {ServiceItem} from '../../domain/service-item';
import {HttpClient} from '@angular/common/http';

@Injectable()
export class SearchService extends Service {

  constructor(http: HttpClient) {
    super(http);
  }

  search(query: String): Promise<ServiceItem[]> {
    return this.get('search?query=' + query);
  }
}
