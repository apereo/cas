import React, { Fragment, useEffect } from 'react';

import { and, rankWith, uiTypeIs } from '@jsonforms/core';
import { withJsonFormsControlProps } from '@jsonforms/react';

export const HashSetRenderer = ({
  schema,
  path,
  handleChange,
  ...props
}) => {
  

  return (
   <Fragment></Fragment>
  );
};

export const isHashSetControl = and(
  uiTypeIs('Control'),
  (uischema, schema, context) => {
    console.log(uischema, schema);
  }
);

export const hashSetControlTester = rankWith(
  10,
  isHashSetControl
);

export default withJsonFormsControlProps(HashSetRenderer);

