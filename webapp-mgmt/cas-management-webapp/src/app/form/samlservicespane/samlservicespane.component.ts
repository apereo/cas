import {Component, OnInit} from '@angular/core';
import {Messages} from '../../messages';
import {SamlRegisteredService} from '../../../domain/saml-service';
import {Data} from '../data';
import {Util} from '../../util/util';
import {Row, RowDataSource} from '../row';

@Component({
  selector: 'app-samlservicespane',
  templateUrl: './samlservicespane.component.html',
  styleUrls: ['./samlservicespane.component.css']
})
export class SamlservicespaneComponent implements OnInit {

  displayedColumns = ['source', 'mapped', 'delete'];
  dataSource: RowDataSource;

  type: String;

  constructor(public messages: Messages,
              public data: Data) {
  }

  ngOnInit() {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService;
    const rows = [];
    if (Util.isEmpty(service.attributeNameFormats)) {
      service.attributeNameFormats = new Map();
    }
    for (const p of Array.from(Object.keys(service.attributeNameFormats))) {
      rows.push(new Row(p));
    }
    this.dataSource = new RowDataSource(rows);
  }

  addRow() {
    this.dataSource.addRow();
  }

  doChange(row: Row, val: string) {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService;
    service.attributeNameFormats[val] = service.attributeNameFormats[row.key as string];
    delete service.attributeNameFormats[row.key as string];
    row.key = val;
  }

  delete(row: Row) {
    const service: SamlRegisteredService = this.data.service as SamlRegisteredService
    delete service.attributeNameFormats[row.key as string];
    this.dataSource.removeRow(row);
  }
}

