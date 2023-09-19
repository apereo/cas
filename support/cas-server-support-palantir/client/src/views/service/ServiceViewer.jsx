import React, { Fragment, useCallback, useEffect } from 'react';
import { NavLink, useNavigate, useParams } from 'react-router-dom';

import { ServiceLoader } from './ServiceLoader';
import { Button } from '@mui/material';
import ArrowBackIcon from '@mui/icons-material/ArrowBack';
import { useSnackbar } from 'notistack';
import { useUpdateServiceMutation } from '../../store/ServiceApi';

import { CodeEditor } from '../../components/CodeEditor';

export function ServiceViewer () {

    const { enqueueSnackbar } = useSnackbar();

    const params = useParams();
    const { id } = params;

    const [updateService, updateResult] = useUpdateServiceMutation();
    const navigate = useNavigate();

    const update = useCallback((s) => {
        console.log(s);
        updateService(s);
    }, [updateService]);

    useEffect(() => {
        const { isSuccess, isUninitialized, isError } = updateResult;
        if (!isUninitialized) {
            if (isSuccess) {
                enqueueSnackbar('Service Updated', { variant: 'success' });
                navigate('/services');
            } else if (isError) {
                const { error } = updateResult;
                enqueueSnackbar(`Updated failed: ${error.data}`, { variant: 'error' });
            }
        }
        
    }, [updateResult]);

    return (
        <ServiceLoader id={ id }>
            {service => 
                <CodeEditor code={JSON.stringify(service, null, 2)} onSave={ update }>
                    <Button
                        component={NavLink}
                        to={`/services`}
                        color="primary"
                        sx={{ my: 2 }}>
                            <ArrowBackIcon />&nbsp; Back
                    </Button>
                </CodeEditor>
            }
        </ServiceLoader>
    );
}