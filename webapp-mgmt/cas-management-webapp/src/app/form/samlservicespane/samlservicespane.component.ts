import {Component, Input, OnInit} from '@angular/core';
import {Messages} from "../../messages";
import {AbstractRegisteredService} from "../../../domain/registered-service";
import {SamlRegisteredService} from "../../../domain/saml-service";

@Component({
  selector: 'app-samlservicespane',
  templateUrl: './samlservicespane.component.html',
  styleUrls: ['./samlservicespane.component.css']
})
export class SamlservicespaneComponent implements OnInit {

  @Input()
  service: SamlRegisteredService;

  @Input()
  selectOptions;

  rows: Row[];
  addName: String;
  addValue: String;
  isAdding: boolean;

  type: String;

  constructor(public messages: Messages) { }

  ngOnInit() {
    //this.rows = this.service.properties.map((p) => new Row(p));
  }

  addRow() {
    let p: any = {"name":this.addName, "value":this.addValue};
    let r: Row = new Row(p);
    this.service.properties[p.name] = p;
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
    //this.serviceData.properties.splice(this.serviceData.properties.indexOf(r.property),1);
    this.rows.splice(this.rows.indexOf(r),1);
  }

  populateInputField(r: Row, value: any) {
    if (r.isEditing) {
      r.tmpValue = value;
    } else {
      this.addValue = value;
    }
  }

}

class Row {

  constructor(p: any) {
    this.property = p;
    this.tmpName = p.name;
    this.tmpValue = p.value;
  }
  isEditing: boolean;
  property: any;
  tmpName: String;
  tmpValue: String;
}
