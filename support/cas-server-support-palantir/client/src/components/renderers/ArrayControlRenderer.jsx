import { isObjectArrayControl, isPrimitiveArrayControl, not, or, rankWith } from '@jsonforms/core';
import { Unwrapped } from '@jsonforms/material-renderers';
import { withJsonFormsArrayLayoutProps } from '@jsonforms/react';

const { MaterialArrayControlRenderer } = Unwrapped;

export const ArrayControlRenderer = (props) => {
    return (
        <MaterialArrayControlRenderer {...props} />
    );
};

export const arrayControlTester = rankWith(
    3,
    or(isObjectArrayControl, isPrimitiveArrayControl)
);

export default withJsonFormsArrayLayoutProps(ArrayControlRenderer);