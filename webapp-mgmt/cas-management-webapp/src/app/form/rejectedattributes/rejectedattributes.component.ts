import {Component, OnInit, Input} from '@angular/core';
import {Messages} from "../../messages";
import {Data, PropertyBean} from "../../../domain/form";

@Component({
  selector: 'app-rejectedattributes',
  templateUrl: './rejectedattributes.component.html'
})
export class RejectedattributesComponent implements OnInit {

  @Input()
  serviceData: Data;

  rows: Row[];
  addName: String;
  addValue: String;
  isAdding: boolean;

  constructor(public messages: Messages) { }

  ngOnInit() {
    this.rows = this.serviceData.supportAccess.rejectedAttr.map((p) => new Row(p));
  }

  addRow() {
    let p: PropertyBean = new PropertyBean(this.addName, this.addValue);
    let r: Row = new Row(p);
    this.serviceData.properties.push(p);
    this.rows.push(r);
    this.isAdding = false;
    this.addName = null;
    this.addValue = null;
  }

  cancelAdd() {
    this.isAdding = false;
    this.addValue = null;
    this.addName = null;
  }

  save(r: Row) {
    r.property.name = r.tmpName;
    r.property.value = r.tmpValue;
    r.isEditing = false;
  }

  cancel(r: Row) {
    r.tmpName = r.property.name;
    r.tmpValue = r.property.value;
    r.isEditing = false;
  }

  delete(r: Row) {
    this.serviceData.properties.splice(this.serviceData.properties.indexOf(r.property),1);
    this.rows.splice(this.rows.indexOf(r),1);
  }

}

class Row {

  constructor(p: PropertyBean) {
    this.property = p;
    this.tmpName = p.name;
    this.tmpValue = p.value;
  }
  isEditing: boolean;
  property: PropertyBean;
  tmpName: String;
  tmpValue: String;
}
