import { defaultServiceClass } from '../store/ServiceSlice';
import casServiceUiSchema from './schema/cas-service';

export const serviceTypes = [
    {
        label: 'CAS Registered Service',
        '@class': 'org.apereo.cas.services.CasRegisteredService',
        def: 'CasRegisteredService',
        default: true,
        uiSchema: casServiceUiSchema
    }
];

export const getServiceType = (klass) => serviceTypes.find(s => s['@class'] === klass);

export const getServiceTypeByClass = getServiceType;

export const getDefaultServiceType = () => serviceTypes.find(s => s.default);

export function useUiSchema(type = defaultServiceClass) {
    const { uiSchema } = getServiceType(type);
    return uiSchema;
}

export default serviceTypes;