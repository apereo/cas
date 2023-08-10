import React, { Fragment, useEffect } from 'react';
import { ServiceForm } from './ServiceForm';
import { useGetServiceQuery } from '../../store/ServiceApi';
import { useParams } from 'react-router-dom';
import { useDispatch } from 'react-redux';

import { updateService, useServiceData } from '../../store/ServiceSlice';

export function EditService () {

    const params = useParams();
    const dispatch = useDispatch();
    const service = useServiceData();

    const update = React.useCallback((s) => {
        dispatch(updateService(s));
    }, [dispatch]);

    const { data } = useGetServiceQuery(params.id);

    useEffect(() => {
        if (data) {
            dispatch(updateService(data));
        }
    }, [data]);

    return (
        <Fragment>
            <ServiceForm service={ service } onUpdate={(s, e) => update(s)} />
        </Fragment>
    );
}