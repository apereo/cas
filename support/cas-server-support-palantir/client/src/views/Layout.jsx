import React from 'react';
import { useTheme } from '@emotion/react';
import { AppBar, Box, Button, Grid, Toolbar } from '@mui/material';
import { Link, NavLink, Outlet } from "react-router-dom";
import CASLogo from '../theme/assets/images/cas-logo.png';

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
            <AppBar position="sticky" sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}>
                <Toolbar>
                    <img src={CASLogo} alt="CAS - Central Authentication Service" className="m-2" style={{maxHeight: '40px', marginRight: '40px'}} />
                    <Button
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        sx={[{ mr: 2 }, {
                            '&.active': {
                              backgroundColor: 'rgba(255, 255, 255, 0.1)',
                            },
                          }]}
                        component={NavLink}
                        to="/"
                    >
                        Home
                    </Button>
                    <Button
                        edge="start"
                        color="inherit"
                        aria-label="menu"
                        sx={[{ mr: 2 }, {
                            '&.active': {
                                backgroundColor: 'rgba(255, 255, 255, 0.1)',
                            },
                          }]}
                        component={NavLink}
                        to="/services"
                    >
                        Services
                    </Button>
                </Toolbar>
            </AppBar>
            <Grid sx={{ height: 'calc(100vh - 64px)' }}>
                <Grid item xs={12} sx={{ height: '100%' }}>
                    <Outlet />
                </Grid>
            </Grid>
        </Box>
    );
}