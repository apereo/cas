import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { API_PATH } from '../App.constant';

// Define a schema using a base URL and expected endpoints
export const schemaApi = createApi({
    reducerPath: 'schemaApi',
    baseQuery: fetchBaseQuery({
        baseUrl: `${API_PATH}`,
        prepareHeaders: (headers, { getState }) => {
            headers.set('Content-Type', 'application/json');
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getSchema: builder.query({
            query: (type) => `/schema/${type}`,
            transformResponse: response => {
                response.type = 'object';
                delete response.$schema;
                return response;
            }
        }),
        getUiSchema: builder.query({
            query: (type) => `/data/${type}-ui-schema.json`,
        }),
    }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetSchemaQuery, useGetUiSchemaQuery } = schemaApi;