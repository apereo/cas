import React, { useCallback, useState, useEffect, Fragment } from "react";

import { diff as DiffEditor } from "react-ace";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-github";
import "ace-builds/src-noconflict/ext-language_tools";
import { Box, Button } from "@mui/material";

// import service1 from '../../public/data/service-1.json';
// import service2 from '../../public/data/service-2.json';

const service1 = {};
const service2 = {};

export function CodeDiff ({ code, format = 'json', onSave, children }) {

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
        <Box sx={ { flexGrow: 1, border: '1px solid rgba(0, 0, 0, 0.2)', height: '100%' } }>
            <DiffEditor
                mode={ format }
                theme="github"
                value={ [ JSON.stringify(service1, null, 2), JSON.stringify(service2, null, 2) ] }
                name="ace-editor"
                width="100%"
                height="100%"
                onChange={ update }
                showPrintMargin={false}
            />
        </Box>
    );
}