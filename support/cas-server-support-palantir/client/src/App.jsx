import React from 'react';
import { Provider } from "react-redux";
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";
import { SnackbarProvider } from 'notistack';
import { store } from './store/store';
import CasThemeProvider from './theme/CasThemeProvider';

import { routes } from './views/Routes';

import { API_PATH, APP_PATH } from './App.constant';


const router = createBrowserRouter(
    [
        ...routes
    ],
    {
        basename: `/${APP_PATH}`
    }
);

function App() {
    return (
        <Provider store={store}>
            <CasThemeProvider>
                <SnackbarProvider>
                    <RouterProvider router={router} />
                </SnackbarProvider>
            </CasThemeProvider>
        </Provider>
    )
}

export default App;
