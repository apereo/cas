import React, { useState, useMemo } from 'react';
import { Box, List, ListItem, ListItemButton, ListItemText } from '@mui/material';
import {
    and,
    deriveLabelForUISchemaElement,
    isVisible,
    rankWith,
    uiTypeIs,
} from '@jsonforms/core';
import {
    withJsonFormsLayoutProps,
    withTranslateProps,
} from '@jsonforms/react';
import {
    MaterialLayoutRenderer,
    withAjvProps,
} from '@jsonforms/material-renderers';
import { defaultTranslator } from '@jsonforms/core';

export const isSingleLevelCategorization = and(
    uiTypeIs('Categorization'),
    (uischema) => {
        const categorization = uischema;

        return (
            categorization.elements &&
            categorization.elements.reduce(
                (acc, e) => acc && e.type === 'Category',
                true
            )
        );
    }
);

export const muiSidebarCategorizationTester = rankWith(
    1000,
    isSingleLevelCategorization
);

export const MuiSidebarCategorizationRenderer = (
    props
) => {
    const {
        data,
        path,
        renderers,
        cells,
        schema,
        uischema,
        visible,
        enabled,
        selected,
        onChange,
        ajv,
        t,
    } = props;
    const categorization = uischema;
    const [previousCategorization, setPreviousCategorization] =
        useState(uischema);
    const [activeCategory, setActiveCategory] = useState(selected ?? 0);
    const categories = useMemo(
        () =>
            categorization.elements.filter((category) =>
                isVisible(category, data, undefined, ajv)
            ),
        [categorization, data, ajv]
    );

    if (categorization !== previousCategorization) {
        setActiveCategory(0);
        setPreviousCategorization(categorization);
    }

    const safeCategory =
        activeCategory >= categorization.elements.length ? 0 : activeCategory;

    const childProps = {
        elements: categories[safeCategory] ? categories[safeCategory].elements : [],
        schema,
        path,
        direction: 'column',
        enabled,
        visible,
        renderers,
        cells,
    };

    const onTabChange = (value) => {
        if (onChange) {
            onChange(value, safeCategory);
        }
        setActiveCategory(value);
    };

    const tabLabels = useMemo(() => {
        return categories.map((e) => deriveLabelForUISchemaElement(e, defaultTranslator));
    }, [categories]);

    const drawerWidth = 240;

    return (
        <Box sx={{ display: 'flex' }}>
            <List sx={{ minWidth: `${drawerWidth}px`, borderRight: `1px solid rgba(0, 0, 0, 0.1)`, boxShadow: 0 }}>
                {categories.map((_, idx) => (
                    <ListItem key={idx} disablePadding>
                        <ListItemButton onClick={() => onTabChange(idx)} selected={activeCategory === idx}>
                            <ListItemText primary={tabLabels[idx]} />
                        </ListItemButton>
                    </ListItem>
                ))}
            </List>
            <Box
                component="main"
                sx={{ flexGrow: 1, bgcolor: 'background.default', p: 3 }}
            >
                <MaterialLayoutRenderer {...childProps} key={safeCategory} />
            </Box>
        </Box>
    );
};

export default withAjvProps(
    withTranslateProps(
        withJsonFormsLayoutProps(MuiSidebarCategorizationRenderer)
    )
);