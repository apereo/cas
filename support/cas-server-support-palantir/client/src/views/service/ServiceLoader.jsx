import React, { Fragment, useEffect } from 'react';
import { useGetServiceQuery } from '../../store/ServiceApi';
import { updateService, useServiceData } from '../../store/ServiceSlice';
import { useDispatch } from 'react-redux';
import { LinearProgress } from '@mui/material';
import { useParams } from 'react-router-dom';

export function ServiceLoader ({ children }) {

    

    return (<Fragment>{ children(service) }</Fragment>)
}