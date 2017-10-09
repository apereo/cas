import {Component, OnInit, ViewChild} from '@angular/core';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/observable/fromEvent';
import {ServiceItem} from "../../domain/service-view-bean";
import {DataSource} from "@angular/cdk/collections";
import {MatPaginator, MatSnackBar} from "@angular/material";
import {Messages} from "../messages";
import {ActivatedRoute, ParamMap, Router} from "@angular/router";
import {Location} from "@angular/common";
import {SearchService} from "./SearchService";

@Component({
  selector: 'app-search',
  templateUrl: './search.component.html',
  styleUrls: ['./search.component.css']
})
export class SearchComponent implements OnInit {
  displayedColumns = ['name','serviceId','description'];
  serviceDatabase = new ServiceDatabase();
  dataSource: ServiceDataSource | null;
  query: String;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(public messages: Messages,
              public router: Router,
              public route: ActivatedRoute,
              public location: Location,
              private service: SearchService,
              public snackBar: MatSnackBar) { }

  ngOnInit() {
    this.dataSource = new ServiceDataSource(this.serviceDatabase, this.paginator);
    this.route.paramMap
        .switchMap((params: ParamMap) => this.service.search(params.get('query')))
        .subscribe(resp => this.serviceDatabase.load(resp));
  }

  goBack() {
    this.location.back();
  }

  serviceEdit(id: number) {
    this.router.navigate(['form',id]);
  }

}

export class ServiceDatabase {
  dataChange: BehaviorSubject<ServiceItem[]> = new BehaviorSubject<ServiceItem[]>([]);
  get data(): ServiceItem[] { return this.dataChange.value; }

  constructor() {
  }

  load(services: ServiceItem[]) {
    this.dataChange.next([]);
    for(let service of services) {
      this.addService(service);
    }
  }

  addService(service: ServiceItem) {
    const copiedData = this.data.slice();
    copiedData.push(service);
    this.dataChange.next(copiedData);
  }


}

export class ServiceDataSource extends DataSource<any> {

  constructor(private _serviceDatabase: ServiceDatabase, private _paginator: MatPaginator) {
    super();
  }

  connect(): Observable<ServiceItem[]> {
    const displayDataChanges = [
      this._serviceDatabase.dataChange,
      this._paginator.page,
    ];

    return Observable.merge(...displayDataChanges).map(() => {
      const data = this._serviceDatabase.data.slice();
      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;
      return data.splice(startIndex, this._paginator.pageSize);
    });
  }

  disconnect() {}
}
