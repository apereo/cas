import React from 'react';

import { createSelector, createSlice } from "@reduxjs/toolkit";
import { useSelector } from "react-redux";

export const defaultServiceClass = 'org.apereo.cas.services.CasRegisteredService';

const initialState = {
    service: {
        '@class': defaultServiceClass
    }
};

export const ServiceSlice = createSlice({
    name: 'service',
    initialState,
    reducers: {
        updateService(state, action) {
            state.service = action.payload;
        },
        newService(state, action) {
            state.service = {
                ...initialState.service,
                ...action.payload
            };
        },
    },
});

const stateSelector = (state) => state.service;
const ServiceSelector = createSelector(stateSelector, (state) => state.service);

export function useServiceData () {
    return useSelector(ServiceSelector);
}

export const { updateService, newService } = ServiceSlice.actions;
