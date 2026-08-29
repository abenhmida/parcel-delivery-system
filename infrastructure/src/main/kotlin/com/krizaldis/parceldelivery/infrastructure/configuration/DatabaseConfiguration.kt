package com.krizaldis.parceldelivery.infrastructure.configuration

import com.zaxxer.hikari.HikariDataSource
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(DatabaseProperties::class)
class DatabaseConfiguration {
    @Bean
    fun dataSource(properties: DatabaseProperties): DataSource =
        HikariDataSource().apply {
            jdbcUrl = properties.url
            username = properties.username
            password = properties.password
        }

    @Bean
    fun dslContext(dataSource: DataSource): DSLContext = DSL.using(dataSource, SQLDialect.POSTGRES)
}
