import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { API_PATH } from '../App.constant';

// Define a schema using a base URL and expected endpoints
export const schemaApi = createApi({
    reducerPath: 'schemaApi',
    baseQuery: fetchBaseQuery({ baseUrl: `${API_PATH}` }),
    endpoints: (builder) => ({
        getSchema: builder.query({
            query: (type) => `data/${type}-schema.json`,
        }),
    }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetSchemaQuery } = schemaApi;