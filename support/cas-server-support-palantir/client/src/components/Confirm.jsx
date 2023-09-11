import * as React from 'react';
import Button from '@mui/material/Button';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

export default function ConfirmDialog({ title = 'Are you sure?', description = 'Are you sure you want to remove this?', open = false, onConfirm, onCancel }) {

  return (
    <div>
      <Dialog
        open={!!open}
        onClose={onCancel}
        aria-labelledby="alert-dialog-title"
        aria-describedby="alert-dialog-description"
      >
        <DialogTitle id="alert-dialog-title">
          {title}
        </DialogTitle>
        <DialogContent>
          <DialogContentText id="alert-dialog-description">
            {description}
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={onCancel}
            variant='outlined'>Cancel</Button>
          <Button onClick={onConfirm} autoFocus
          variant='contained'
          color="error">
            Ok
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  );
}