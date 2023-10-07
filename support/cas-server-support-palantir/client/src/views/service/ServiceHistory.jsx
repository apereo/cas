import React, { useCallback, useState } from 'react';

import { CodeDiff } from '../../components/CodeDiff';
import { DialogContent, Dialog, DialogTitle, Box, DialogActions, Button, IconButton } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import CloseIcon from '@mui/icons-material/Close';

export function ServiceHistory () {
    const navigate = useNavigate();
    const onClose = useCallback(() => {
        navigate("/services");
    });

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
            <DialogContent>
                <Box sx={{height: '80svh'}}>
                    <CodeDiff />
                </Box>
            </DialogContent>
            <DialogActions>
                <Button variant="contained" onClick={onClose}>Close</Button>
            </DialogActions>
        </Dialog>
    );
}