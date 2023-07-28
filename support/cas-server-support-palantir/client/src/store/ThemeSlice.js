import React from 'react';

import { createSelector, createSlice } from "@reduxjs/toolkit";
import { useSelector } from "react-redux";
import { createTheme } from "@mui/material/styles";

import { getDesignTokens } from '../theme/theme';

const Modes = Object.freeze({
    LIGHT: "light",
    DARK: "dark"
});

export const ThemeSlice = createSlice({
    name: 'theme',
    initialState: {
        mode: Modes.LIGHT
    },
    reducers: {
        setMode(state, action) {
            state.mode = action.payload;
        },
    },
});

const stateSelector = (state) => state.theme;
const ModeSelector = createSelector(stateSelector, (state) => state.mode);

export function useCasThemeMode () {
    return useSelector(ModeSelector).toString();
}

export function useCasTheme () {
    const mode = useCasThemeMode();

    return React.useMemo(
        () => createTheme(getDesignTokens(mode)),
        [mode]
    );
}