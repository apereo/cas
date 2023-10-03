import React, { Fragment, useMemo, useEffect } from 'react';
import { useGetServiceQuery } from '../../store/ServiceApi';
import { updateService, useServiceData } from '../../store/ServiceSlice';
import { useDispatch } from 'react-redux';
import { LinearProgress } from '@mui/material';

export function ServiceLoader ({ id, children }) {

    const { data = {}, isFetching, isError } = useGetServiceQuery(id);

    const dispatch = useDispatch();

    useEffect(() => {
        if (data.id) {
            dispatch(updateService(data));
        }
    }, [data, dispatch]);


    const service = useServiceData();

    if (isError) return <div>An error has occurred!</div>

    if (isFetching) return <LinearProgress />

    return (<Fragment>{ children(service) }</Fragment>)
}