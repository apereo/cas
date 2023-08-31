import { Services } from './Services';
import { ServiceList } from './ServiceList';
import { ServiceEditor } from './ServiceEditor';
import { Link as RouterLink } from 'react-router-dom';
import { Link } from '@mui/material';
import { ServiceCreator } from './ServiceCreator';

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