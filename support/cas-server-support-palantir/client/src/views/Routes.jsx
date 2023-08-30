import Layout from "./Layout";

import services from './service';
import dashboard from './dashboard';

export const routes = [
    {
        path: '/',
        element: <Layout />,
        children: [
            dashboard,
            services,
        ]
    }
]