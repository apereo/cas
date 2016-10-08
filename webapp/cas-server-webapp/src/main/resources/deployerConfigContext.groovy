beans {
    xmlns([context:'http://www.springframework.org/schema/context'])
    xmlns([lang:'http://www.springframework.org/schema/lang'])
    xmlns([util:'http://www.springframework.org/schema/util'])
    xmlns([metrics:'http://www.ryantenney.com/schema/metrics'])

    metrics.'annotation-driven'('metric-registry':'metrics','health-check-registry':'healthCheckMetrics')
}
