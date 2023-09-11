import { cyan, teal, grey } from "@mui/material/colors";
import { alpha, lighten } from '@mui/material';

export const getDesignTokens = (mode) => ({
    typography: {
        // In Chinese and Japanese the characters are usually larger,
        // so a smaller fontsize may be appropriate.
        fontSize: 15,
        fontFamily: [
            "Droid Sans",
            '"Segoe UI"',
            '"Helvetica Neue"',
            "Arial",
            "sans-serif",
            '"Apple Color Emoji"',
            '"Segoe UI Emoji"',
            '"Segoe UI Symbol"',
        ].join(","),
    },
    palette: {
        mode,
        ...(mode === "light"
            ? {
                  // palette values for light mode
                primary: {
                    main: '#153e50',
                    light: '#436473',
                    dark: '#0e2b38',
                    contrastText: '#FFFFFF'
                },
                divider: grey[800],
                text: {
                    primary: grey[900],
                    secondary: grey[800],
                },
              }
            : {
                  // palette values for dark mode
                  primary: {
                      light: cyan[200],
                      main: cyan[400],
                      dark: "#004c5d",
                      contrastText: "#fff",
                  },
                  divider: grey[800],
                  background: {
                      default: grey[900],
                      paper: grey[900],
                      info: alpha(grey[800], 0.4),
                  },
                  text: {
                      primary: "#fff",
                      secondary: grey[300],
                  },
              }),
    },
    components: {
        MuiButton: {
            styleOverrides: {
                root: ({ ownerState }) => ({
                    ...(ownerState.variant === "contained" &&
                        ownerState.color === "primary" && {
                            backgroundColor: "#006d85",
                        }),
                }),
            },
        },
    },
});