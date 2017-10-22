import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {DataSource} from "@angular/cdk/collections";
import {MatPaginator} from "@angular/material";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import 'rxjs/add/operator/debounceTime';
import 'rxjs/add/operator/distinctUntilChanged';
import 'rxjs/add/observable/fromEvent';

export class Database<T> {
  dataChange: BehaviorSubject<T[]> = new BehaviorSubject<T[]>([]);
  get data(): T[] { return this.dataChange.value; }

  constructor() {
  }

  load(items: T[]) {
    this.dataChange.next([]);
    for(let item of items) {
      this.addItem(item);
    }
  }

  addItem(item: T) {
    const copiedData = this.data.slice();
    copiedData.push(item);
    this.dataChange.next(copiedData);
  }

}

export class Datasource<T> extends DataSource<T> {
  _filterChange = new BehaviorSubject('');
  get filter(): string { return this._filterChange.value; }
  set filter(filter: string) { this._filterChange.next(filter); }

  constructor(private _domainDatabase: Database<T>, private _paginator: MatPaginator, private filterFn?: any) {
    super();
    if (!filterFn) {
      this.filterFn = (value: T) => { return value};
    }
  }

  connect(): Observable<T[]> {
    const displayDataChanges = [
      this._domainDatabase.dataChange,
      this._filterChange,
      this._paginator.page,
    ];

    return Observable.merge(...displayDataChanges).map(() => {
      const data = this._domainDatabase.data.slice().filter((value: T) => this.filterFn(value,this.filter));
      const startIndex = this._paginator.pageIndex * this._paginator.pageSize;
      return data.splice(startIndex, this._paginator.pageSize);
    });
  }

  disconnect() {}
}