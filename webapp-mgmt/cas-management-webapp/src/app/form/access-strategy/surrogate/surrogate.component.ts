import { Component, OnInit } from '@angular/core';
import {SurrogateRegisteredServiceAccessStrategy} from '../../../../domain/access-strategy';
import {Messages} from '../../../messages';
import {Data} from '../../data';
import {Util} from '../../../util/util';
import {Row, RowDataSource} from '../../row';

@Component({
  selector: 'app-surrogate',
  templateUrl: './surrogate.component.html',
  styleUrls: ['./surrogate.component.css']
})
export class SurrogateComponent implements OnInit {

  accessStrategy: SurrogateRegisteredServiceAccessStrategy;

  displayedColumns = ['source', 'mapped', 'delete'];
  dataSource: RowDataSource;

  constructor(public messages: Messages,
              private data: Data) {
    this.accessStrategy = data.service.accessStrategy as SurrogateRegisteredServiceAccessStrategy;
  }

  ngOnInit() {
    const rows = [];
    if (Util.isEmpty(this.accessStrategy.surrogateRequiredAttributes)) {
      this.accessStrategy.surrogateRequiredAttributes = new Map();
    }
    for (const p of Array.from(Object.keys(this.accessStrategy.surrogateRequiredAttributes))) {
      rows.push(new Row(p));
    }
    this.dataSource = new RowDataSource(rows);
  }

  addRow() {
    this.dataSource.addRow();
  }

  doChange(row: Row, val: string) {
    this.accessStrategy.surrogateRequiredAttributes[val] = this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    delete this.accessStrategy.surrogateRequiredAttributes[row.key as string];
    this.dataSource.removeRow(row);
  }
}

