import {Component, Input, OnInit} from '@angular/core';
import {ServiceDetails} from "../../../domain/service-view-bean";
import {Messages} from "../../messages";

@Component({
  selector: 'app-details',
  templateUrl: './details.component.html',
  styleUrls: ['./details.component.css']
})
export class DetailsComponent implements OnInit {

  @Input()
  detailRow: ServiceDetails;

  constructor(public messages: Messages) { }

  ngOnInit() {
  }

}
