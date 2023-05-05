import React from 'react';
import { Provider } from "react-redux";
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";

import { store } from './store/store';
import CasThemeProvider from './theme/CasThemeProvider';
import Layout from './Layout';

import { dashboardRoutes } from './dashboard/routes';

const router = createBrowserRouter([
    ...dashboardRoutes
]);

function App() {
    return (
        <Provider store={store}>
            <CasThemeProvider>
                <Layout>
                    <RouterProvider router={router} />
                </Layout>
            </CasThemeProvider>
        </Provider>
    )
}

export default App
