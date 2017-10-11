import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {DataSource} from '@angular/cdk/collections';
import {MatPaginator, MatSnackBar} from '@angular/material';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {Observable} from 'rxjs/Observable';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/observable/fromEvent';
import {DomainService} from "./domain.service";
import {Messages} from "app/messages";
import {Router} from "@angular/router";
import {Location} from "@angular/common";

@Component({
  selector: 'app-domains',
  templateUrl: './domains.component.html',
  styleUrls: ['./domains.component.css']
})
export class DomainsComponent implements OnInit {
  displayedColumns = ['row'];
  domainDatabase = new DomainDatabase();
  dataSource: DomainDataSource | null;
  selectedItem: String;

  @ViewChild(MatPaginator) paginator: MatPaginator;

  constructor(public messages: Messages,
              private router: Router,
              private domainService: DomainService,
              public snackBar: MatSnackBar,
              private location: Location) { }

  ngOnInit() {
    this.dataSource = new DomainDataSource(this.domainDatabase, this.paginator);
    this.domainService.getDomains()
      .then(resp => this.domainDatabase.load(resp))
      .catch(e => this.snackBar.open("Failed to load domains", "Dismiss"));
  }

  doFilter(val: string) {
    if (!this.dataSource) { return; }
    this.dataSource.filter = val;
  }

  view(domain: String) {
    this.router.navigate(['services', domain]);
  }

  goBack() {
    this.location.back();
  }

}

export class DomainDatabase {
  dataChange: BehaviorSubject<String[]> = new BehaviorSubject<String[]>([]);
  get data(): String[] { return this.dataChange.value; }

  constructor() {
  }

  load(domains: String[]) {
    for(let domain of domains) {
      this.addDomain(domain);
    }
  }

  addDomain(domain: String) {
    const copiedData = this.data.slice();
    copiedData.push(domain);
    this.dataChange.next(copiedData);
  }



}

export class DomainDataSource extends DataSource<any> {
  _filterChange = new BehaviorSubject('');
  get filter(): string { return this._filterChange.value; }
  set filter(filter: string) { this._filterChange.next(filter); }

  constructor(private _domainDatabase: DomainDatabase, private _paginator: MatPaginator) {
    super();
  }

  connect(): Observable<String[]> {
    const displayDataChanges = [
      this._domainDatabase.dataChange,
      this._filterChange,
      this._paginator.page,
    ];

    return Observable.merge(...displayDataChanges).map(() => {
      const data = this._domainDatabase.data.slice().filter((item: String) => {
        let searchStr = item.toLowerCase();
        return searchStr.indexOf(this.filter.toLowerCase()) != -1;
      });

      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;
      return data.splice(startIndex, this._paginator.pageSize);
    });
  }

  disconnect() {}
}
