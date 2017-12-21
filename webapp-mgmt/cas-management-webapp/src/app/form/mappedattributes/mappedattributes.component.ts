import {ChangeDetectorRef, Component, Input, OnInit} from '@angular/core';
import 'rxjs/add/operator/startWith';
import 'rxjs/add/observable/merge';
import 'rxjs/add/operator/map';
import {Data} from '../data';
import {FormData} from '../../../domain/form-data';
import {Messages} from '../../messages';
import {Row, RowDataSource} from '../row';

@Component({
  selector: 'app-mappedattributes',
  templateUrl: './mappedattributes.component.html',
  styleUrls: ['./mappedattributes.component.css']
})
export class MappedattributesComponent implements OnInit {
  formData: FormData;
  displayedColumns = ['source', 'mapped'];
  dataSource: RowDataSource;

  @Input()
  attributes: Map<String, String[]>;


  constructor(public messages: Messages,
              public data: Data) {
    this.formData = data.formData;
  }

  ngOnInit() {
      const rows = [];
      for (const p of Array.from(Object.keys(this.attributes))) {
          rows.push(new Row(p));
      }
      this.dataSource = new RowDataSource(rows);
  }

}

