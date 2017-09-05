/**
 * Created by tsschmi on 2/28/17.
 */
import {NgModule} from "@angular/core";
import {CommonModule} from "@angular/common";
import {FormsModule} from "@angular/forms"
import 'hammerjs';
import {MyTooltipDirective} from "../my-tooltip.directive";
import {
  MdButtonModule, MdCardModule,
  MdCheckboxModule, MdDatepickerModule, MdDialogModule, MdExpansionModule, MdCard, MdIcon, MdIconModule, MdInputModule,
  MdNativeDateModule,
  MdRadioModule, MdRippleModule,
  MdSelectModule, MdTableModule,
  MdTabsModule,
  MdTooltipModule, MdListModule, MdCoreModule, MdMenuModule, MdChipsModule, MdAutocompleteModule, MdSnackBarModule
} from "@angular/material";
import {CdkTableModule} from "@angular/cdk/table";

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
    MdMenuModule,
    MdChipsModule,
    MdDialogModule,
    MdAutocompleteModule,
    MdSnackBarModule
  ],
  declarations: [
    MyTooltipDirective,
  ],
  providers: [
  ],
  exports: [
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
    MdMenuModule,
    MdChipsModule,
    MdDialogModule,
    MdAutocompleteModule,
    MdSnackBarModule
  ]
})
export class SharedModule {
  static empty(obj: any): boolean {
    return !obj || Object.keys(obj).length == 0;
  }
}
