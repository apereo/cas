import React from 'react';
import { Provider } from "react-redux";
import {
    createBrowserRouter,
    RouterProvider,
} from "react-router-dom";

import { store } from './store/store';
import CasThemeProvider from './theme/CasThemeProvider';

import { routes } from './views/Routes';

import { APP_PATH } from './App.constant';

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
                <RouterProvider router={router} />
            </CasThemeProvider>
        </Provider>
    )
}

export default App;
