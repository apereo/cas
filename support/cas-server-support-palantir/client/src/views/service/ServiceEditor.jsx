import React, { Fragment, useCallback, useEffect } from 'react';
import { ServiceLoader } from './ServiceLoader';
import { ServiceTypeSelector } from './ServiceTypeSelector';
import { ServiceForm } from './ServiceForm';

import { useUpdateServiceMutation } from '../../store/ServiceApi';
import { useNavigate, useParams } from 'react-router-dom';
import { useSnackbar } from 'notistack';

export function ServiceEditor () {

    const { enqueueSnackbar } = useSnackbar();

    const params = useParams();
    const { id } = params;

    const [updateService, updateResult] = useUpdateServiceMutation();
    const navigate = useNavigate();

    const update = useCallback((s) => {
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
        <Fragment>
            <ServiceLoader id={ id }>
                {service => <ServiceTypeSelector service={ service }>
                    {schema => <ServiceForm uiSchema={ schema } service={ service } onSave={ update } />}
                </ServiceTypeSelector>}
            </ServiceLoader>
        </Fragment>
    );
}