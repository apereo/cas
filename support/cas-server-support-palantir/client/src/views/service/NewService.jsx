import React, { Fragment, useCallback, useEffect } from 'react';
import { ServiceForm } from './ServiceForm';

import { useDispatch } from 'react-redux';

import { updateService, newService, useServiceData } from '../../store/ServiceSlice';

export function NewService () {
    const dispatch = useDispatch();
    const service = useServiceData();

    const update = React.useCallback((s) => {
        dispatch(updateService(s));
    }, [dispatch]);

    useEffect(() => {
        dispatch(newService({}));
    }, []);

    return (
        <Fragment>
            <ServiceForm service={ service } onUpdate={ (s, e) => update(s) } />
        </Fragment>
    );
}