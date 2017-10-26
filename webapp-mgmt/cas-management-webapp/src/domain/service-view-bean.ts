/**
 * Created by tschmidt on 2/13/17.
 */
import {RegisteredServiceProperty} from './property';

export class ServiceItem {
  evalOrder: number;
  assignedId: String;
  serviceId: String;
  name: String;
  description: String;
}

export class FormData {
  availableAttributes: String[] = [];
  registeredServiceProperties: RegisteredServiceProperty[]
}

