import React, { useState, useMemo, Fragment } from 'react';
import { Box, Divider, Drawer, IconButton, List, ListItem, ListItemButton, ListItemText, Toolbar, Typography } from '@mui/material';
import { styled, useTheme } from '@mui/material/styles';
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
import ChevronLeftIcon from '@mui/icons-material/ChevronLeft';
import ChevronRightIcon from '@mui/icons-material/ChevronRight';

const drawerWidth = 240;

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

const DrawerHeader = styled('div')(({ theme }) => ({
    display: 'flex',
    alignItems: 'center',
    padding: theme.spacing(0, 1),
    // necessary for content to be below app bar
    ...theme.mixins.toolbar,
    justifyContent: 'flex-end',
  }));

const Main = styled('div', { shouldForwardProp: (prop) => prop !== 'open' })(
    ({ theme, open }) => ({
      flexGrow: 1,
      padding: theme.spacing(3),
      transition: theme.transitions.create('margin', {
        easing: theme.transitions.easing.sharp,
        duration: theme.transitions.duration.leavingScreen,
      }),
      marginLeft: `-${drawerWidth}px`,
      ...(open && {
        transition: theme.transitions.create('margin', {
          easing: theme.transitions.easing.easeOut,
          duration: theme.transitions.duration.enteringScreen,
        }),
        marginLeft: 0,
      }),
    }),
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
        config,
        t,
    } = props;

    const { context } = config;
    const { open, setOpen } = context;

    const theme = useTheme();
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

    const handleDrawerClose = () => {
        setOpen(false);
    };

    return (
        <Box sx={{ display: 'flex' }}>
            <Drawer
                variant="persistent"
                sx={{
                    width: drawerWidth,
                    flexShrink: 0,
                    [`& .MuiDrawer-paper`]: { width: drawerWidth, boxSizing: 'border-box' },
                }}
                open={open}
            >
                <Toolbar />
                <DrawerHeader>
                    <IconButton onClick={handleDrawerClose}>
                        {theme.direction === 'rtl' ? <ChevronRightIcon /> : <ChevronLeftIcon />}
                    </IconButton>
                </DrawerHeader>
                <Divider light />
                <Box sx={{ display: 'flex' }}>
                    <List>
                        {categories.map((_, idx) => (
                            <ListItem key={idx} disablePadding>
                                <ListItemButton onClick={() => onTabChange(idx)} selected={activeCategory === idx}>
                                    <ListItemText primary={tabLabels[idx]} />
                                </ListItemButton>
                            </ListItem>
                        ))}
                    </List>
                </Box>
            </Drawer>
            <Main open={open}
                sx={{ flexGrow: 1, bgcolor: 'background.default', p: 3 }}
            >
                <MaterialLayoutRenderer {...childProps} key={safeCategory} />
            </Main>
        </Box>
    );
};

export default withAjvProps(
    withTranslateProps(
        withJsonFormsLayoutProps(MuiSidebarCategorizationRenderer)
    )
);