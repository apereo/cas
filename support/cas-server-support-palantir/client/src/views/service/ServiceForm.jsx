import React, { Fragment, useCallback } from 'react';
import { JsonForms } from '@jsonforms/react';
import { materialRenderers } from '@jsonforms/material-renderers';
import { useGetSchemaQuery, useGetUiSchemaQuery } from '../../store/SchemaApi';

import { muiSidebarCategorizationTester, MuiSidebarCategorizationRenderer } from '../../components/renderers/MuiSidebarCategorizationRenderer';

const renderers = [
    ...materialRenderers,
    { tester: muiSidebarCategorizationTester, renderer: MuiSidebarCategorizationRenderer }
]

export function ServiceForm ({ service, onUpdate }) {

    const { data: schema } = useGetSchemaQuery('service');
    const { data: uiSchema } = useGetUiSchemaQuery('service');
    
    const update = useCallback((data, errors) => {
        onUpdate(data, errors);
    }, []);

    return (
        <Fragment>
            { schema && uiSchema &&
                <JsonForms
                    renderers={renderers}
                    uischema={uiSchema}
                    schema={schema}
                    data={service}
                    onChange={({ errors, data }) => update(data)}
                />
            }
        </Fragment>
    );
}