import {MatTableDataSource} from '@angular/material';

export class Row {
  key: String;

  constructor (key: String) {
    this.key = key;
  }
}

export class RowDataSource extends MatTableDataSource<Row> {

  addRow(row?: Row) {
    this.data.push(row ? row : new Row(''));
    this._updateChangeSubscription();
  }

  removeRow(row: Row) {
    this.data.splice(this.data.indexOf(row), 1);
    this._updateChangeSubscription();
  }

}
