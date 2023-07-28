import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { API_PATH } from '../App.constant';

// Define a service using a base URL and expected endpoints
export const serviceApi = createApi({
    reducerPath: 'serviceApi',
    baseQuery: fetchBaseQuery({ baseUrl: `${API_PATH}` }),
    endpoints: (builder) => ({
        getServices: builder.query({
            query: () => `data/service.json`,
        }),
    }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const { useGetServicesQuery } = serviceApi