/**
 * Created by tsschmi on 2/28/17.
 */
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms"
import {AlertComponent} from "../alert/alert.component";
import 'hammerjs';
import {MyTooltipDirective} from "../my-tooltip.directive";
import {
  MdButtonModule, MdCardModule,
  MdCheckboxModule, MdDatepickerModule, MdDialogModule, MdExpansionModule, MdCard, MdIcon, MdIconModule, MdInputModule,
  MdNativeDateModule,
  MdRadioModule, MdRippleModule,
  MdSelectModule, MdTableModule,
  MdTabsModule,
  MdTooltipModule, MdListModule, MdCoreModule, MdMenuModule, MdChipsModule
} from "@angular/material";
import {CdkTableModule} from "@angular/cdk";

@NgModule({
  imports: [
    CommonModule,
    FormsModule,
    MdTabsModule,
    MdCheckboxModule,
    MdInputModule,
    MdIconModule,
    MdTooltipModule,
    MdSelectModule,
    MdDatepickerModule,
    MdNativeDateModule,
    MdDialogModule,
    MdTableModule,
    CdkTableModule,
    MdButtonModule,
    MdRadioModule,
    MdRippleModule,
    MdCardModule,
    MdExpansionModule,
    MdListModule,
    MdCoreModule,
    MdMenuModule,
    MdChipsModule
  ],
  declarations: [
    AlertComponent,
    MyTooltipDirective,
  ],
  providers: [
  ],
  exports: [
    AlertComponent,
    MyTooltipDirective,
    MdTabsModule,
    MdCheckboxModule,
    MdInputModule,
    MdIconModule,
    MdTooltipModule,
    MdSelectModule,
    MdDatepickerModule,
    MdNativeDateModule,
    MdDialogModule,
    MdTableModule,
    CdkTableModule,
    MdButtonModule,
    MdRadioModule,
    MdRippleModule,
    MdCardModule,
    MdExpansionModule,
    MdListModule,
    MdCoreModule,
    MdMenuModule,
    MdChipsModule
  ]
})
export class SharedModule {
  static empty(obj: any): boolean {
    return !obj || Object.keys(obj).length == 0;
  }
}
