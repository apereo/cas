import React, { Fragment, useEffect } from 'react';

import { and, rankWith, uiTypeIs, schemaMatches } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';

export const MuiConstRenderer = ({
  schema,
  path,
  handleChange,
  ...props
}) => {
  
  useEffect(() => {
    handleChange(path, schema.const);
  }, [schema, path, handleChange])

  useEffect(() => {
    // console.log(props, schema, path);
  }, [props, schema, path])

  return (
   <Fragment></Fragment>
  );
};

export const isConstControl = and(
  uiTypeIs('Control'),
  schemaMatches((schema) => 
    Object.prototype.hasOwnProperty.call(schema, 'const')
  )
);

export const muiConstControlTester = rankWith(
  10,
  isConstControl
);

export default withJsonFormsControlProps(MuiConstRenderer);

