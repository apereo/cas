import React from 'react';
import { useTheme } from '@emotion/react';
import { AppBar, Box, Button, Grid, Toolbar } from '@mui/material';

export default function Layout ({ children }) {
    const theme = useTheme();
    return (
        <Box
            sx={{
                display: "flex",
                flexDirection: "column",
                width: "100vw",
                height: "auto",
                minHeight: "100vh",
                bgcolor: theme.palette.background.default,
                color: theme.palette.text.primary,
            }}
        >
            <AppBar position="static">
                <Toolbar>
                    <Button
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        sx={{ mr: 2 }}
                    >
                        Services
                    </Button>
                </Toolbar>
            </AppBar>
            <Grid
                container
                direction="column"
                alignItems="center"
            >
                <Grid item xs={12}>
                    {children}
                </Grid>
            </Grid>
        </Box>
    );
}