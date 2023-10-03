import React, { useCallback, useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { DialogContent, Dialog, DialogTitle, Box, DialogActions, Button, IconButton, LinearProgress } from '@mui/material';
import CloseIcon from '@mui/icons-material/Close';
import { useSnackbar } from 'notistack';
import { useGetServiceQuery, useUpdateServiceMutation } from '../../store/ServiceApi';

import { CodeEditor } from '../../components/CodeEditor';
import { useDispatch } from 'react-redux';

export function ServiceViewer () {
    const navigate = useNavigate();
    const dispatch = useDispatch();
    const { id } = useParams();
    const onClose = useCallback(() => {
        navigate("/services");
    });
    const { enqueueSnackbar } = useSnackbar();

    const [updateService, updateResult] = useUpdateServiceMutation();
    const { data, isFetching, isError } = useGetServiceQuery(id);
    const [ error, setError ] = useState(false);
    
    const [ service, setService ] = useState(data);
    const [ changes, setChanges ] = useState(data);

    const save = useCallback((s) => {
        updateService(s)
    }, [updateService]);

    const update = useCallback((changes, invalid, raw) => {
        setError(invalid);
        if (!invalid) {
            setChanges(changes);
        }
    }, []);

    useEffect(() => {
        setService(data);
        setChanges(data);
    }, [data]);

    useEffect(() => {
        const { isSuccess, isUninitialized, isError } = updateResult;
        if (!isUninitialized) {
            if (isSuccess) {
                enqueueSnackbar('Service Updated', { variant: 'success' });
                onClose();
            } else if (isError) {
                const { error } = updateResult;
                enqueueSnackbar(`Updated failed: ${error.data}`, { variant: 'error' });
            }
        }
        
    }, [updateResult]);

    if (isError) {
        return <div>An error has occurred!</div>
    };

    if (isFetching) {
        return <LinearProgress />
    };

    return (
        <Dialog
            fullWidth={true}
            maxWidth={'lg'}
            open={true}
            onClose={onClose}
        >
            <DialogTitle>Service History</DialogTitle>
            <IconButton
                aria-label="close"
                onClick={onClose}
                sx={{
                    position: 'absolute',
                    right: 8,
                    top: 8,
                    color: (theme) => theme.palette.grey[500],
                }}
                >
                <CloseIcon />
                </IconButton>
            <DialogContent sx={{p: 0}}>
                <Box sx={{height: '80svh'}}>
                    <CodeEditor data={ service } onChange={ update } />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button onClick={() => onClose()}>Cancel</Button>
                <Button variant="contained" disabled={error} onClick={() => save(changes)}>Save</Button>
            </DialogActions>
        </Dialog>
        
    );
}