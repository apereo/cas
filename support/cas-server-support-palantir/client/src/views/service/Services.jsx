import React, { Fragment } from 'react';
import { NavLink, Outlet } from 'react-router-dom';
import { AppBar, Button, Container, Divider, Grid, Toolbar } from '@mui/material';
import { useTheme } from '@emotion/react';
import { Crumbs } from '../../components/Crumbs';

export function Services () {

    const theme = useTheme();

    return (
        <Grid sx={ { height: '100%', display: 'flex', flexGrow: 1, flexDirection: 'column' } }>
            <Toolbar variant='dense' sx={{ justifyContent: 'space-between', boxShadow: 2, bgcolor: 'primary.light', color: 'primary.contrastText' }}>
                <Crumbs />
            </Toolbar>
            <Outlet />
        </Grid>
    );
}