import React from 'react';

import { createSelector, createSlice } from "@reduxjs/toolkit";
import { useSelector } from "react-redux";

export const ServiceSlice = createSlice({
    name: 'service',
    initialState: {
        service: {}
    },
    reducers: {
        updateService(state, action) {
            state.service = action.payload;
        },
    },
});

const stateSelector = (state) => state.service;
const ServiceSelector = createSelector(stateSelector, (state) => state.service);

export function useServiceData () {
    return useSelector(ServiceSelector);
}

export const { updateService } = ServiceSlice.actions;