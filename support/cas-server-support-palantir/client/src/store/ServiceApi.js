import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { API_PATH } from '../App.constant';
import { formatToRead, formatToSave } from '../data/format';

// Define a service using a base URL and expected endpoints
export const serviceApi = createApi({
    reducerPath: 'serviceApi',
    baseQuery: fetchBaseQuery({ baseUrl: `${API_PATH}` }),
    tagTypes: ['Service'],
    endpoints: (builder) => ({
        getServices: builder.query({
            query: () => `/services`,
            transformResponse: (response) => formatToRead({services: response}).services
                .map(s => ({...s, id: BigInt(s.id).toString()})),
            providesTags: ['Service']
        }),
        getService: builder.query({
            query: (id) => `/services/${BigInt(id).toString()}`,
            providesTags: ['Service'],
            transformResponse: (response) => formatToRead(response)
        }),
        createService: builder.mutation({
            query: (body) => ({
                url: `services`,
                method: 'POST',
                body: formatToSave(body),
            }),
            invalidatesTags: ['Service']
        }),
        updateService: builder.mutation({
            query: (body) => ({
                url: `services`,
                method: 'PUT',
                body: formatToSave(body),
            }),
            invalidatesTags: ['Service']
        }),
        deleteService: builder.mutation({
            query: (id) => ({
                url: `services/${id}`,
                method: 'DELETE'
            }),
            invalidatesTags: ['Service']
        })
    }),
})

// Export hooks for usage in functional components, which are
// auto-generated based on the defined endpoints
export const {
    useGetServicesQuery,
    useGetServiceQuery,
    useCreateServiceMutation,
    useUpdateServiceMutation,
    useDeleteServiceMutation
} = serviceApi;