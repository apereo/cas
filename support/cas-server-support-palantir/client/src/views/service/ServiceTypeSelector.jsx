import React, { Fragment, useMemo, useState } from 'react';

import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Dialog from '@mui/material/Dialog';
import DialogActions from '@mui/material/DialogActions';
import DialogContent from '@mui/material/DialogContent';
import DialogContentText from '@mui/material/DialogContentText';
import DialogTitle from '@mui/material/DialogTitle';

import serviceTypes, { getServiceType, getDefaultServiceType } from '../../data/service-types';
import { Autocomplete } from '@mui/material';

const def = getDefaultServiceType();

export function ServiceTypeSelector ({ service, children }) {
    const klass = useMemo(() => {
        return service.hasOwnProperty('@class') ? service['@class'] : null;
    }, [service]);

    const [type, setType] = useState(getServiceType(klass));
    const [selected, setSelected] = useState();

    const handleClose = () => {
        setType(selected);
    };

    const schema = useMemo(() => type ? type.uiSchema : null, [type]);

    return (type ? 
        <Fragment>{ children(schema) }</Fragment>
        :
        <div>
            <Dialog open={!type}>
                <DialogTitle>Select Service Type</DialogTitle>
                <DialogContent>
                    <Autocomplete
                        disablePortal
                        onChange={(event, newValue) => {
                            setSelected(newValue);
                        }}
                        getOptionLabel={(opt) => opt.label}
                        id="service-type-selector"
                        options={serviceTypes}
                        sx={{ width: 300 }}
                        renderInput={(params) => <TextField {...params} label="Service Type" />}
                    />
                </DialogContent>
                <DialogActions>
                    <Button onClick={handleClose}>Select</Button>
                </DialogActions>
            </Dialog>
        </div>
    );
}

export default ServiceTypeSelector;