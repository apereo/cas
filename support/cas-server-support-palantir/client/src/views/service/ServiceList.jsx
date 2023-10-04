import React, { Fragment, useCallback, useEffect, useState } from 'react';

import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import { Button, Container, Divider, Typography } from '@mui/material';
import { NavLink, Outlet } from 'react-router-dom';
import { useSnackbar } from 'notistack';

import EditIcon from '@mui/icons-material/Edit';
import DeleteForeverIcon from '@mui/icons-material/DeleteForever';
import CodeIcon from '@mui/icons-material/Code';
import HistoryIcon from '@mui/icons-material/History';

import ConfirmDialog from '../../components/Confirm';
import { getServiceType } from '../../data/service-types';
import { useDeleteServiceMutation, useGetServicesQuery } from '../../store/ServiceApi';


export function ServiceList () {
    const { data = [], error, isLoading } = useGetServicesQuery();
    const { enqueueSnackbar } = useSnackbar();

    const columns = [
        { label: 'ID', prop: 'id' },
        { label: 'Name', prop: 'name', component: 'th' },
        { label: 'Type', prop: 'type' },
    ];

    const [removeService, removeServiceResult] = useDeleteServiceMutation();

    const [deleting, setDeleting] = useState();

    const onConfirm = useCallback(() => {
        removeService(deleting);
    }, [deleting]);

    const onCancel = useCallback(() => {
        setDeleting();
    }, []);

    useEffect(() => {
        const {isSuccess} = removeServiceResult;
        if (isSuccess) {
            setDeleting();
            enqueueSnackbar('Serviced removed successfully!', { variant: 'success' });
        }
    }, [removeServiceResult]);

    return (
        <Fragment>
            <ConfirmDialog open={ deleting } onCancel={onCancel} onConfirm={onConfirm} />
            <Container sx={{p: 1}}>
                <Divider sx={{ my: 2 }} light={ true } />
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
                <Divider sx={{ my: 2 }} light={ true } />
                {data.length ?
                <TableContainer component={Paper}>
                    <Table sx={{}} aria-label="simple table">
                        <TableHead>
                            <TableRow>
                                <TableCell component={'th'}>
                                    Service ID
                                </TableCell>
                                <TableCell component={'th'}>
                                    Name
                                </TableCell>
                                <TableCell component={'th'}>
                                    Type
                                </TableCell>
                                <TableCell></TableCell>
                            </TableRow>
                        </TableHead>
                        <TableBody>
                        {data.map((row, idx) => (
                            <TableRow
                                key={idx}
                                sx={{ '&:last-child td, &:last-child th': { border: 0 } }}
                            >
                                <TableCell component={'th'} scope="row">
                                    {row.serviceId}
                                </TableCell>
                                <TableCell component={'th'}>
                                    {row.name}
                                </TableCell>
                                <TableCell component={'th'}>
                                    {getServiceType(row['@class'])?.label}
                                </TableCell>
                                <TableCell align="right">
                                    <Button component={NavLink} to={`/services/${row.id}/view`}
                                        variant='contained'
                                        color="warning"
                                        sx={{mx: 1}}>
                                            <CodeIcon />&nbsp; View
                                    </Button>
                                    {/*<Button component={NavLink} to={`/services/${row.id}/diff`}
                                        variant='contained'
                                        color="info"
                                        sx={{mx: 1}}>
                                            <HistoryIcon />&nbsp; History
                                    </Button>*/}
                                    <Button component={NavLink} to={`/services/${row.id}`}
                                        variant='contained'
                                        color="primary"
                                        sx={{mx: 1}}>
                                            <EditIcon />&nbsp; Edit
                                    </Button>
                                    <Button onClick={ () => setDeleting(row.id) }
                                        variant='contained'
                                        color="error"
                                        sx={{mx: 1}}>
                                            <DeleteForeverIcon />&nbsp; Delete
                                    </Button>
                                </TableCell>
                            </TableRow>
                        ))}
                        </TableBody>
                    </Table>
                </TableContainer>
                :
                <Typography>There are no services defined.</Typography>
                }
            </Container>
            <Outlet />
        </Fragment>
    );
}

export default ServiceList;