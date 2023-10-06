import { Link as RouterLink } from 'react-router-dom';
import { Link } from '@mui/material';

import { Services } from './Services';
import { ServiceList } from './ServiceList';
import { ServiceEditor } from './ServiceEditor';
import { ServiceCreator } from './ServiceCreator';
import { ServiceViewer } from './ServiceViewer';
import { ServiceHistory } from './ServiceHistory';
import { ServiceLoader } from './ServiceLoader';

export default {
    path: "services",
    element: <Services />,
    handle:{
        crumb: () => <Link to="/services" underline="hover" color="inherit" component={RouterLink}>Services</Link>,
    },
    children: [
        {
            path: '',
            element: <ServiceList />,
            children: [
                {
                    path: ':id/diff',
                    element: <ServiceHistory />
                },
                {
                    path: ':id/view',
                    element: <ServiceViewer />
                }
            ]
        },
        {
            path: 'new',
            element: <ServiceCreator />
        },
        {
            path: ':id',
            element: <ServiceEditor />
        }
    ]
};