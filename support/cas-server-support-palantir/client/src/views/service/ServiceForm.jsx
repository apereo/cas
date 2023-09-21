import React, { useCallback, useEffect, useState } from 'react';
import { JsonForms } from '@jsonforms/react';
import { materialRenderers, materialCells } from '@jsonforms/material-renderers';
import { useGetSchemaQuery } from '../../store/SchemaApi';

import MuiSidebarCategorizationRenderer, { muiSidebarCategorizationTester } from '../../components/renderers/MuiSidebarCategorizationRenderer';
import { defaultServiceClass, updateService, useServiceData } from '../../store/ServiceSlice';
import { useUiSchema } from '../../data/service-types';
import { Button, Divider, Grid, Toolbar } from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import { NavLink } from 'react-router-dom';
import MuiAnyOfRenderer, { muiAnyOfControlTester } from '../../components/renderers/MuiAnyOfRenderer';
import MuiConstRenderer, { muiConstControlTester } from '../../components/renderers/MuiConstRenderer';
import HashSetRenderer, { hashSetControlTester } from '../../components/renderers/HashSetRenderer';


const renderers = [
    { tester: muiSidebarCategorizationTester, renderer: MuiSidebarCategorizationRenderer },
    { tester: muiAnyOfControlTester, renderer: MuiAnyOfRenderer },
    { tester: muiConstControlTester, renderer: MuiConstRenderer },
    ...materialRenderers,
];

export function ServiceForm ({ service, onSave, type = defaultServiceClass }) {

    const [data, setData] = useState({
        '@class': type,
        ...service
    });

    const [errors, setErrors] = useState();

    const { data: schema } = useGetSchemaQuery('services');
    const uiSchema = useUiSchema(type);
    
    const update = useCallback((s, errors) => {
        setData({
            '@class': type,
            ...s,
        });
        setErrors(errors);
    }, [setData]);

    const save = useCallback((d) => {
        onSave(d);
    }, [data]);

    useEffect(() => {
        updateService(data);   
    }, [data]);

    return (
        <Grid>
            <Toolbar sx={{ justifyContent: 'end' }}>
                <Button
                    variant='outlined'
                    edge="start"
                    aria-label="menu"
                    sx={{mr: 2}}
                    component={NavLink}
                    to="/services"
                >
                    Cancel
                </Button>
                <Button
                    variant='contained'
                    edge="start"
                    color="primary"
                    aria-label="menu"
                    disabled={ errors?.length > 0 }
                    onClick={ () => save(data) }
                >
                    <SaveIcon />&nbsp; Save
                </Button>
            </Toolbar>
            <Divider light />
            { schema && uiSchema &&
                <JsonForms
                    renderers={renderers}
                    cells={materialCells}
                    uischema={uiSchema}
                    schema={schema}
                    data={service}
                    onChange={({ errors, data }) => update(data, errors)}
                    // ajv={ ajv }
                />
            }
        </Grid>
    );
}