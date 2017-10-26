import {Component, Input, OnInit} from '@angular/core';
import {RegisteredServiceMappedRegexAttributeFilter} from '../../../../domain/attribute-filter';
import {BehaviorSubject} from 'rxjs/BehaviorSubject';
import {DataSource} from '@angular/cdk/collections';
import {Observable} from 'rxjs/Observable';
import {Data} from '../../data';
import {Messages} from '../../../messages';

@Component({
  selector: 'app-mapped',
  templateUrl: './mapped.component.html',
  styleUrls: ['./mapped.component.css']
})
export class MappedComponent implements OnInit {
    displayedColumns = ['source', 'mapped', 'delete'];
    attributeDatabase = new AttributeDatabase();
    dataSource: AttributeDataSource | null;
    formData;

    @Input('filter')
    filter: RegisteredServiceMappedRegexAttributeFilter;

  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
      this.dataSource = new AttributeDataSource(this.attributeDatabase);
      if (this.filter.patterns) {
          for (const p of Array.from(Object.keys(this.filter.patterns))) {
              this.attributeDatabase.addRow(new Row(p));
          }
      }
  }

    addRow() {
        this.attributeDatabase.addRow(new Row(''));
    }

    doChange(row: Row, val: string) {
        console.log(row.key + ' : ' + val);
        this.filter.patterns[val] = this.filter.patterns[row.key as string];
        delete this.filter.patterns[row.key as string];
        row.key = val;
    }

    delete(row: Row) {
        delete this.filter.patterns[row.key as string];
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
        copiedData.splice(copiedData.indexOf(row), 1);
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
