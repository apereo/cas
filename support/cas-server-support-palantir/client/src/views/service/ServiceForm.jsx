import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { JsonForms } from '@jsonforms/react';
import { materialRenderers, materialCells } from '@jsonforms/material-renderers';
import { useGetSchemaQuery } from '../../store/SchemaApi';

import MuiSidebarCategorizationRenderer, { muiSidebarCategorizationTester } from '../../components/renderers/MuiSidebarCategorizationRenderer';
import { defaultServiceClass, updateService } from '../../store/ServiceSlice';
import { useUiSchema } from '../../data/service-types';
import { Box, Button, Divider, Grid, IconButton, Toolbar } from '@mui/material';
import SaveIcon from '@mui/icons-material/Save';
import MenuIcon from '@mui/icons-material/Menu';
import { NavLink } from 'react-router-dom';
import MuiAnyOfRenderer, { muiAnyOfControlTester } from '../../components/renderers/MuiAnyOfRenderer';
import MuiConstRenderer, { muiConstControlTester } from '../../components/renderers/MuiConstRenderer';
import TupleRenderer, { tupleControlTester } from '../../components/renderers/TupleRenderer';
import ArrayControlRenderer, { arrayControlTester } from '../../components/renderers/ArrayControlRenderer';
import ArrayLayoutRenderer, { arrayLayoutTester } from '../../components/renderers/ArrayLayoutRenderer';

const renderers = [
    { tester: muiSidebarCategorizationTester, renderer: MuiSidebarCategorizationRenderer },
    { tester: muiAnyOfControlTester, renderer: MuiAnyOfRenderer },
    { tester: muiConstControlTester, renderer: MuiConstRenderer },
    { tester: tupleControlTester, renderer: TupleRenderer },
    { tester: arrayControlTester, renderer: ArrayControlRenderer },
    { tester: arrayLayoutTester, renderer: ArrayLayoutRenderer },
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

    const [open, setOpen] = React.useState(true);

    const config = useMemo(() => ({
        context: {
            open,
            setOpen
        }
    }), [open]);

    return (
        <Grid>
            <Toolbar sx={{ justifyContent: 'space-between' }}>
                <IconButton
                    color="inherit"
                    aria-label="open drawer"
                    onClick={() => setOpen(!open)}
                    edge="start"
                    sx={{ mr: 2 }}
                >
                    <MenuIcon />
                </IconButton>
                <Box>
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
                </Box>
            </Toolbar>
            <Divider light />
            { schema && uiSchema &&
                <JsonForms
                    renderers={renderers}
                    cells={materialCells}
                    uischema={uiSchema}
                    schema={schema}
                    data={service}
                    config={config}
                    onChange={({ errors, data }) => update(data, errors)}
                />
            }
        </Grid>
    );
}