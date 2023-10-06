import React, { useCallback, useState, useEffect, useMemo } from "react";

import AceEditor from "react-ace";

import "ace-builds/src-noconflict/mode-json";
import "ace-builds/src-noconflict/theme-github";
import "ace-builds/src-noconflict/ext-language_tools";
import { Box } from "@mui/material";

export function CodeEditor ({ data, format = 'json', onChange, onError }) {

    const code = useMemo(() => JSON.stringify(data, null, 2));

    const [current, setCurrent] = useState(code);

    useEffect(() => {
        setCurrent(code);
    }, [code]);

    const change = useCallback((d) => {
        let updates, error;
        try {
            updates = JSON.parse(d);
        } catch (e) {
            error = e;
        }
        onChange(updates, !!error, d);
        setCurrent(d);
    }, [onChange])

    return (
        <Box sx={ { height: '100%', display: 'flex', flexDirection: 'column' } }>
            <Box sx={ { flexGrow: 1, border: '1px solid rgba(0, 0, 0, 0.2)' } }>
                <AceEditor
                    mode={ format }
                    theme="github"
                    value={ current }
                    name="ace-editor"
                    width="100%"
                    height="100%"
                    onChange={ (d) => change(d) }
                    showPrintMargin={false}
                />
            </Box>
        </Box>
    );
}