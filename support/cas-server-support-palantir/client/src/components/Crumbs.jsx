import React, { Fragment } from 'react';

import { Breadcrumbs, Typography, Link } from "@mui/material";
import {
    Link as RouterLink,
    useLocation,
    useMatches,
} from 'react-router-dom';

function LinkRouter(props) {
    return <Link {...props} component={RouterLink} />;
}

export function Crumbs() {
    let matches = useMatches();
    let crumbs = matches
        .filter((match) => Boolean(match.handle?.crumb))
        .map((match) => match.handle.crumb(match.data));
  
    return (
        <Breadcrumbs aria-label="breadcrumb" sx={{color: 'inherit'}}>
            <LinkRouter underline="hover" color="inherit" to="/">
                Home
            </LinkRouter>
            {crumbs.map((crumb, index) => 
                <div key={index}>{crumb}</div>
            )}
        </Breadcrumbs>
    );
}

export default Crumbs;