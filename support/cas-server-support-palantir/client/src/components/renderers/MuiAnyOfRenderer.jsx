import React, { useCallback, useEffect, useState } from 'react';

import {
  createCombinatorRenderInfos,
  isAnyOfControl,
  rankWith,
} from '@jsonforms/core';
import { JsonFormsDispatch, withJsonFormsAnyOfProps } from '@jsonforms/react';
import { Box, FormControl, Hidden, InputLabel, MenuItem, Select, Tab, Tabs } from '@mui/material';
import { CombinatorProperties } from '@jsonforms/material-renderers';
import { createAnyOfRenderInfos } from '../../jsonschema/createAnyOfRenderInfos';

export const MuiAnyOfRenderer = ({
  schema,
  rootSchema,
  indexOfFittingSchema,
  visible,
  path,
  renderers,
  cells,
  uischema,
  uischemas,
  ...props
}) => {
  const [selectedAnyOf, setSelectedAnyOf] = useState(indexOfFittingSchema || 0);
  const handleChange = useCallback(
    (_ev, value) => {
      setSelectedAnyOf(_ev.target.value);
    },
    [setSelectedAnyOf]
  );
  const anyOf = 'anyOf';
  const anyOfRenderInfos = createAnyOfRenderInfos(
    (schema).anyOf,
    rootSchema,
    anyOf,
    uischema,
    path,
    uischemas
  );

  return (
    <Hidden xsUp={!visible}>
      <CombinatorProperties
        schema={schema}
        combinatorKeyword={anyOf}
        path={path}
      />
      <Box sx={{ p: 2, my: 2, border: '1px dashed grey' }}>
      <FormControl fullWidth>
        <InputLabel id={ `select-label-${selectedAnyOf}` }>Type</InputLabel>
        <Select
          labelId={ `select-label-${selectedAnyOf}` }
          id={ `select-${selectedAnyOf}` }
          value={selectedAnyOf}
          label="Type"
          onChange={handleChange}
        >
          {anyOfRenderInfos.map((anyOfRenderInfo, idx) => (
            <MenuItem key={idx} value={idx}>{anyOfRenderInfo.label}</MenuItem>
          ))}
        </Select>
      </FormControl>
      {anyOfRenderInfos.map(
        (anyOfRenderInfo, anyOfIndex) =>
          selectedAnyOf === anyOfIndex && (
            <JsonFormsDispatch
              key={anyOfIndex}
              schema={anyOfRenderInfo.schema}
              uischema={anyOfRenderInfo.uischema}
              path={path}
              renderers={renderers}
              cells={cells}
            />
          )
      )}
      </Box>
    </Hidden>
  );
};

export const muiAnyOfControlTester = rankWith(
  10,
  isAnyOfControl
);

export default withJsonFormsAnyOfProps(MuiAnyOfRenderer);

