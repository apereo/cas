import { Services } from './Services';
import { ListServices } from './ListServices';
import { NewService } from './NewService';
import { EditService } from './EditService';
import { Link as RouterLink } from 'react-router-dom';
import { Typography, Link } from '@mui/material';

export default {
    path: "/services",
    element: <Services />,
    handle:{
        // you can put whatever you want on a route handle
        // here we use "crumb" and return some elements,
        // this is what we'll render in the breadcrumbs
        // for this route
        crumb: () => <Link to="/services" underline="hover" color="inherit" component={RouterLink}>Services</Link>,
    },
    children: [
        {
            path: '',
            element: <ListServices />,
        },
        {
            path: 'new',
            element: <NewService />,
            handle: {
                crumb: () => <Typography>New Service</Typography>,
            }
        },
        {
            path: ':id',
            element: <EditService />,
            handle: {
                crumb: () => <Typography>Edit Service</Typography>,
            }
        }
    ]
};