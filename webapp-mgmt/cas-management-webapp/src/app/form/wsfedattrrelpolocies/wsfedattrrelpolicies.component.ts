import {Component, Input, OnInit} from '@angular/core';
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {Data} from "../data";
import {DataSource} from "@angular/cdk/table";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {WSFederationRegisterdService} from "../../../domain/wsed-service";
import {WsFederationClaimsReleasePolicy} from "../../../domain/attribute-release";
import {Util} from "../../util/util";


@Component({
  selector: 'app-wsfedattrrelpolicies',
  templateUrl: './wsfedattrrelpolicies.component.html',
  styleUrls: ['./wsfedattrrelpolicies.component.css']
})
export class WsfedattrrelpoliciesComponent implements OnInit {

  formData: FormData;
  selectOptions;
  wsFedOnly: boolean;

  displayedColumns = ['source','mapped'];
  attributeDatabase = new AttributeDatabase();
  dataSource: AttributeDataSource | null;


  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
    this.selectOptions = data.selectOptions;
  }

  ngOnInit() {
    let attrPolicy: WsFederationClaimsReleasePolicy = this.data.service.attributeReleasePolicy as WsFederationClaimsReleasePolicy;
    if(Util.isEmpty(attrPolicy.allowedAttributes)) {
      attrPolicy.allowedAttributes = new Map();
    }

    this.formData.availableAttributes.forEach((k) => {
      attrPolicy.allowedAttributes[k as string] = k;
    });

    for(let key of Array.from(Object.keys(attrPolicy.allowedAttributes))) {
      this.attributeDatabase.addRow(new Row(key as string));
    };

    this.dataSource = new AttributeDataSource(this.attributeDatabase);
  }

  isEmpty(data: any[]): boolean {
    return !data || data.length == 0;
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
