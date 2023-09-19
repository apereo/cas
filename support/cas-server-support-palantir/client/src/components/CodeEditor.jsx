import React, { useCallback, useState, useEffect, Fragment } from "react";

import AceEditor from "react-ace";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-github";
import "ace-builds/src-noconflict/ext-language_tools";
import { Box, Button } from "@mui/material";
import SaveIcon from '@mui/icons-material/Save';

export function CodeEditor ({ code, format = 'json', onSave, children }) {

    const [data, setData] = useState(code);

    const save = useCallback((d) => {
        onSave(d);
    }, [data]);

    const update = useCallback((value) => {
        setData(value);
    }, [setData]);

    useEffect(() => {
        setData(code);
    }, [code])

    return (
        <Box sx={ { px: 1, height: '100%', display: 'flex', flexDirection: 'column' } }>
            <Box sx={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                <Fragment>{children}</Fragment>
                <Button
                    variant='contained'
                    color="primary"
                    aria-label="menu"
                    onClick={ () => onSave(data) }
                >
                    <SaveIcon />&nbsp; Save
                </Button>
            </Box>
            <Box sx={ { flexGrow: 1, border: '1px solid rgba(0, 0, 0, 0.2)' } }>
                <AceEditor
                    mode={ format }
                    theme="github"
                    value={ data }
                    name="ace-editor"
                    width="100%"
                    height="100%"
                    onChange={ update }
                    showPrintMargin={false}
                />
            </Box>
        </Box>
    );
}