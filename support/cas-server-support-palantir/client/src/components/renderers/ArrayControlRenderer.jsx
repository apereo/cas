import { isObjectArrayControl, isPrimitiveArrayControl, or, rankWith } from '@jsonforms/core';
import { Unwrapped } from '@jsonforms/material-renderers';
import { withJsonFormsArrayLayoutProps } from '@jsonforms/react';
import { useEffect } from 'react';

const { MaterialArrayControlRenderer } = Unwrapped;

export const ArrayControlRenderer = (props) => {
    useEffect(() => { console.log('ArrayControlRenderer', props) }, [props]);
    return (
        <MaterialArrayControlRenderer {...props} />
    );
};

export const arrayControlTester = rankWith(
    1000,
    or(isObjectArrayControl, isPrimitiveArrayControl)
  );

export default withJsonFormsArrayLayoutProps(ArrayControlRenderer);