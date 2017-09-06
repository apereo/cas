import { Component, OnInit } from '@angular/core';
import {SurrogateRegisteredServiceAccessStrategy} from "../../../../domain/access-strategy";
import {Messages} from "../../../messages";
import {Data} from "../../data";
import {Util} from "../../../util/util";
import {DataSource} from "@angular/cdk/collections";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';

@Component({
  selector: 'app-surrogate',
  templateUrl: './surrogate.component.html',
  styleUrls: ['./surrogate.component.css']
})
export class SurrogateComponent implements OnInit {

  accessStrategy: SurrogateRegisteredServiceAccessStrategy;

  displayedColumns = ['source', 'mapped', "delete"];
  attributeDatabase = new AttributeDatabase();
  dataSource: AttributeDataSource | null;

  constructor(public messages: Messages,
              private data: Data) {
    this.accessStrategy = data.service.accessStrategy as SurrogateRegisteredServiceAccessStrategy;
  }

  ngOnInit() {
    if (Util.isEmpty(this.accessStrategy.surrogateRequiredAttributes)) {
      this.accessStrategy.surrogateRequiredAttributes = new Map();
    }
    for (let p of Array.from(Object.keys(this.accessStrategy.surrogateRequiredAttributes))) {
      this.attributeDatabase.addRow(new Row(p));
    }
    this.dataSource = new AttributeDataSource(this.attributeDatabase);

  }

  addRow(){
    this.attributeDatabase.addRow(new Row(""));
  }

  doChange(row: Row, val: string) {
    this.accessStrategy.surrogateRequiredAttributes[val] = this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    this.attributeDatabase.removeRow(row);
  }

}

export class Row {
  key: String;

  constructor(source: String) {
    this.key = source;
  }
}

export class AttributeDatabase {
  dataChange: BehaviorSubject<Row[]> = new BehaviorSubject<Row[]>([]);
  get data(): Row[] { return this.dataChange.value; }

  constructor() {
  }

  addRow(row: Row) {
    const copiedData = this.data.slice();
    copiedData.push(row);
    this.dataChange.next(copiedData);
  }

  removeRow(row: Row) {
    const copiedData = this.data.slice();
    copiedData.splice(copiedData.indexOf(row),1);
    this.dataChange.next(copiedData);
  }
}

export class AttributeDataSource extends DataSource<any> {
  constructor(private _attributeDatabase: AttributeDatabase) {
    super();
  }

  connect(): Observable<Row[]> {
    return this._attributeDatabase.dataChange;
  }

  disconnect() {}
}
