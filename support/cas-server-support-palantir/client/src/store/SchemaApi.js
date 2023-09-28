import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react'
import { API_PATH } from '../App.constant';

// Define a schema using a base URL and expected endpoints
export const schemaApi = createApi({
    reducerPath: 'schemaApi',
    baseQuery: fetchBaseQuery({
        baseUrl: `${API_PATH}`,
        // baseUrl: `/data`,
        prepareHeaders: (headers, { getState }) => {
            headers.set('Content-Type', 'application/json');
            return headers;
        }
    }),
    endpoints: (builder) => ({
        getSchema: builder.query({
            query: (type) => `/schema/${type}`,
            // query: (type) => `service-schema.json`,
            transformResponse: response => {
                // response.type = 'object';
                delete response.$schema;
                delete response.required;
                delete response.properties;
                response.anyOf = [
                    ...response.anyOf.filter(ao => ao.$ref.match('CasRegisteredService'))
                ];
                const str = JSON.stringify(response).replaceAll('"const"', '"type": "string", "const"').replaceAll('$1', '');
                const parsed = JSON.parse(str);
                // console.log(parsed);
                return parsed;
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