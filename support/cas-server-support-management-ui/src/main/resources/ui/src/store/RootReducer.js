import { combineReducers } from "@reduxjs/toolkit";

import { serviceApi } from './ServiceApi';
import { schemaApi } from "./SchemaApi";
import { ThemeSlice } from "./ThemeSlice";
import { ServiceSlice } from "./ServiceSlice";

const reducer = combineReducers({
    theme: ThemeSlice.reducer,
    service: ServiceSlice.reducer,
    [serviceApi.reducerPath]: serviceApi.reducer,
    [schemaApi.reducerPath]: schemaApi.reducer
});


export default reducer;
