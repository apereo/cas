import React from 'react';
import { ThemeProvider } from "@mui/material/styles";
import { useCasTheme } from '../store/ThemeSlice';

export default function CasThemeProvider ({ children }) {
    const theme = useCasTheme();

    return (
        <ThemeProvider theme={theme}>{ children }</ThemeProvider>
    );
}