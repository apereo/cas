import { configureStore } from "@reduxjs/toolkit";

import rootReducer from "./RootReducer";
import { serviceApi } from "./ServiceApi";
import { schemaApi } from "./SchemaApi";

export const store = configureStore({
    reducer: rootReducer,
    middleware: (getDefaultMiddleware) => 
        getDefaultMiddleware()
        .prepend()
        .concat([
            serviceApi.middleware,
            schemaApi.middleware
        ]),
});
