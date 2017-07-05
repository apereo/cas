import { Component, OnInit, ViewChild } from '@angular/core';
import ServiceView from "../../domain/service-view";
import {Messages} from "../messages";
import {ActivatedRoute, Router} from "@angular/router";
import {ServiceViewService} from "./service.service";
import {Location} from "@angular/common";
import {AlertComponent} from "../alert/alert.component";

@Component({
  selector: 'app-services',
  templateUrl: './services.component.html',
  styleUrls: ['./services.component.css']
})
export class ServicesComponent implements OnInit {

  @ViewChild('alert')
  alert: AlertComponent;
  dataTable: ServiceView[];
  detailRow: String;
  deleteItem: ServiceView;
  domain: String;

  constructor(public messages: Messages,
              private route: ActivatedRoute,
              private router: Router,
              private service: ServiceViewService,
              private location: Location) {
    this.dataTable = [];
  }

  ngOnInit() {
    this.route.data
      .subscribe((data: { resp: ServiceView[]}) => {
        this.dataTable = data.resp;
        if (!this.dataTable) {
          this.alert.show(this.messages.management_services_status_listfail,'danger');
        }
      });
  }

  serviceEdit(selectedItem: String) {
    console.log("selecteditem = " + selectedItem);
    this.router.navigate(['/form',selectedItem]);
  }

  serviceDuplicate(selectedItem: String) {
    this.router.navigate(['/form',selectedItem, {duplicate: true}]);
  }

  toggleDetail(id: String) {
    this.detailRow = id;
  }

  openModalDelete(selectedItem: ServiceView) {
    this.deleteItem = selectedItem;
  };

  closeModalDelete() {
    this.deleteItem  = null;
  };

  delete() {
    let myData = {id: this.deleteItem.assignedId};

    this.service.delete(Number.parseInt(this.deleteItem.assignedId as string))
      .then(resp => this.handleDelete())
      .catch((e: any) => this.alert.show(this.messages.management_services_status_notdeleted, 'danger'));
    this.closeModalDelete();
  };

  handleDelete() {
    this.alert.show(this.messages.management_services_status_deleted,'info');
    this.refresh();
  }

  refresh() {
    setTimeout(() => {
      this.getServices();
    },500);
  }

  getServices() {
    this.service.getServices()
      .then(resp => this.dataTable = resp)
      .catch((e: any) => this.alert.show(this.messages.management_services_status_listfail,'danger'));
  }

  goBack() {
    this.location.back();
  }

  moveUp(a: ServiceView) {
    let index: number = this.dataTable.indexOf(a);
    if(index > 0) {
      let b: ServiceView = this.dataTable[index-1];
      a.evalOrder = index-1;
      b.evalOrder = index;
      this.dataTable[index] = b;
      this.dataTable[index -1] = a;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  moveDown(a: ServiceView) {
    let index: number = this.dataTable.indexOf(a);
    if(index < this.dataTable.length -1) {
      let b: ServiceView = this.dataTable[index+1];
      a.evalOrder = index+1;
      b.evalOrder = index;
      this.dataTable[index] = b;
      this.dataTable[index+1] = a;
      this.service.updateOrder(a,b).then(resp => this.refresh());
    }
  }

  needsPadding(item) {
    return this.dataTable.indexOf(item) != 0 || this.dataTable.indexOf(item ) == this.dataTable.length - 1 ? '2px' : '0px';
  }

}
