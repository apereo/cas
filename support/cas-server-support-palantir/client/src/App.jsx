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

import { API_PATH } from './App.constant';

const router = createBrowserRouter([
    ...dashboardRoutes
]);

function App() {

    console.log(import.meta.env.API_PATH)

    return (
        <Provider store={store}>
            <CasThemeProvider>
                <Layout>
                    { API_PATH }
                    <RouterProvider router={router} />
                </Layout>
            </CasThemeProvider>
        </Provider>
    )
}

export default App;
