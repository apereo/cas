import React, { Fragment } from 'react';
import { JsonForms } from '@jsonforms/react';
import { materialRenderers } from '@jsonforms/material-renderers';
import { useDispatch } from 'react-redux';
import { useGetSchemaQuery } from '../store/SchemaApi';

import { updateService, useServiceData } from '../store/ServiceSlice';

export function Dashboard () {

    const { data: schema } = useGetSchemaQuery('service');
    const dispatch = useDispatch();

    const service = useServiceData();

    const update = React.useCallback((s) => {
        dispatch(updateService(s));
    }, [dispatch]);

    React.useEffect(() => console.log(service), [service]);

    return (
        <Fragment>
            { schema &&
                <JsonForms
                    renderers={materialRenderers}
                    schema={schema}
                    data={service}
                    onChange={({ errors, data }) => update(data)}
                />
            }
        </Fragment>
    );
}