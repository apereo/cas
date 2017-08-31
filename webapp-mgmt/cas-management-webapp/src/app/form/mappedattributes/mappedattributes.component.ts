import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import {DataSource} from "@angular/cdk/table";
import {BehaviorSubject} from "rxjs/BehaviorSubject";
import {Observable} from "rxjs/Observable";
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Data} from "../data";
import {FormData} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";

@Component({
  selector: 'app-mappedattributes',
  templateUrl: './mappedattributes.component.html',
  styleUrls: ['./mappedattributes.component.css']
})
export class MappedattributesComponent implements OnInit {
  formData: FormData;
  displayedColumns = ['source','mapped'];
  attributeDatabase = new AttributeDatabase();
  dataSource: AttributeDataSource | null;

  @Input()
  attributes: Map<String, String[]>;


  constructor(public messages: Messages,
              public data: Data,
              private changeDetector: ChangeDetectorRef) {
    this.formData = data.formData;
  }

  ngOnInit() {
    for(let key of Array.from(Object.keys(this.attributes))) {
      this.attributeDatabase.addRow(new Row(key as string));
    };

    this.dataSource = new AttributeDataSource(this.attributeDatabase);
    this.changeDetector.detectChanges();
  }

}

export class Row {
  source: String;

  constructor(source: String) {
    this.source = source;
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
