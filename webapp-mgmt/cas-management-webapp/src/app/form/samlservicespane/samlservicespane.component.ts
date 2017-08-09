import {Component, Input, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {SamlRegisteredService} from "../../../domain/saml-service";
import {Data} from "../data";
import {DataSource} from "@angular/cdk";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Util} from "../../util/util";

@Component({
  selector: 'app-samlservicespane',
  templateUrl: './samlservicespane.component.html',
  styleUrls: ['./samlservicespane.component.css']
})
export class SamlservicespaneComponent implements OnInit {

  service: SamlRegisteredService;
  selectOptions;
  displayedColumns = ['source', 'mapped', "delete"];
  attributeDatabase = new AttributeDatabase();
  dataSource: AttributeDataSource | null;

  type: String;

  constructor(public messages: Messages,
              private data: Data) {
    this.service = data.service as SamlRegisteredService;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    if (Util.isEmpty(this.service.attributeNameFormats)) {
      this.service.attributeNameFormats = new Map();
    }
    for (let p of Array.from(Object.keys(this.service.attributeNameFormats))) {
      this.attributeDatabase.addRow(new Row(p));
    }
    this.dataSource = new AttributeDataSource(this.attributeDatabase);
  }

  addRow(){
    this.attributeDatabase.addRow(new Row(""));
  }

  doChange(row: Row, val: string) {
    this.service.properties[val] = this.service.properties[row.key as string];
    delete this.service.properties[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    delete this.service.properties[row.key as string];
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
