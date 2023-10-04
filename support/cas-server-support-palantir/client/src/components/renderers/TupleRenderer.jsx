import React, { Fragment, useMemo } from 'react';

import { JsonFormsDispatch, withJsonFormsControlProps } from '@jsonforms/react';
import { and, rankWith, schemaMatches, schemaTypeIs, uiTypeIs } from '@jsonforms/core';
import { createTupleRenderInfos } from '../../jsonschema/createTupleRenderInfos';
import { Typography } from '@mui/material';

export const TupleRenderer = (props) => {
  const {
    rootSchema,
    indexOfFittingSchema,
    visible,
    path,
    renderers,
    cells,
    uischema,
    uischemas,
    schema
  } = props;
  
  // const schema = resolveSchema(rootSchema, uischema.scope, rootSchema);

  const renderInfo = useMemo(() => createTupleRenderInfos(
    schema.prefixItems,
    rootSchema,
    uischema,
    path,
    uischemas
  ), [schema, rootSchema, uischema, path, uischemas]);


  return (
    <Fragment>
      {uischema.label && <Typography variant={'h6'}>{ uischema.label }</Typography>}
      {renderInfo.map(
        (ri, idx) =>
          (
            <JsonFormsDispatch
              key={idx}
              schema={ri.schema}
              uischema={ri.uischema}
              path={ri.path}
              renderers={renderers}
              cells={cells}
            />
          )
      )}
    </Fragment>
  );
};

export const isTupleControl = and(
  uiTypeIs('Control'),
  schemaTypeIs('array'),
  schemaMatches((schema) => Object.prototype.hasOwnProperty.call(schema, 'prefixItems'))
);

export const tupleControlTester = rankWith(
  10,
  isTupleControl
);

export default withJsonFormsControlProps(TupleRenderer);

