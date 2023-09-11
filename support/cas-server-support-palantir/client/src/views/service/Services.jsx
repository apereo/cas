import React, { useMemo } from 'react';
import { Outlet, useMatches } from 'react-router-dom';
import { Divider, Grid, Toolbar } from '@mui/material';
import { Crumbs } from '../../components/Crumbs';

export function Services () {
    return (
        <Grid sx={ { height: '100%', display: 'flex', flexGrow: 1, flexDirection: 'column' } }>
            <Outlet />
        </Grid>
    );
}