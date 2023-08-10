import React, { useMemo } from 'react';
import { useGetServicesQuery } from '../../store/ServiceApi';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Button, Container, Divider, Grid, Link } from '@mui/material';
import { NavLink } from 'react-router-dom';

export function ListServices () {
    const { data = [], error, isLoading } = useGetServicesQuery();

    const columns = [
        { label: 'ID', prop: 'id' },
        { label: 'Name', prop: 'name', component: 'th' },
        { label: 'type', prop: 'type' },
    ];

    return (
        <Container sx={{p: 1}}>
            <Button
                variant='contained'
                edge="start"
                color="primary"
                aria-label="menu"
                component={NavLink}
                to="/services/new"
            >
                New Service
            </Button>
            <Divider sx={{ my: 2, backgroundColor: 'primary.main' }} />
            <TableContainer component={Paper}>
                <Table sx={{}} aria-label="simple table">
                    <TableHead>
                        <TableRow>
                            {columns.map((column, idx) =>
                            <TableCell key={idx}>{ column.label }</TableCell>
                            )}
                            <TableCell></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                    {data.map((row, idx) => (
                        <TableRow
                            key={idx}
                            sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                        >
                            {columns.map((column, cidx) =>
                                <TableCell component={column.component || 'td'} scope="row" key={cidx}>
                                    {row[column.prop]}
                                </TableCell>
                            )}
                            <TableCell>
                                <Link component={NavLink} to={`/services/${row.id}`}>Edit</Link>
                            </TableCell>
                        </TableRow>
                    ))}
                    </TableBody>
                </Table>
            </TableContainer>
        </Container>
        
    );
}

export default ListServices;