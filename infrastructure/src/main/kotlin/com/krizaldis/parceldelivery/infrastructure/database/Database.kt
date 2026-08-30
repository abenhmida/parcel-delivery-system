package com.krizaldis.parceldelivery.infrastructure.database

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.flywaydb.core.Flyway
import org.jooq.DSLContext
import org.jooq.Data
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import javax.sql.DataSource

data class DatabaseProperties(
    val url: String,
    val username: String,
    val password: String,
)

object DatabaseFactory {
    fun createDataSource(properties: DatabaseProperties): HikariDataSource =
        HikariDataSource(
            HikariConfig().apply {
                jdbcUrl = properties.url
                username = properties.username
                password = properties.password
                maximumPoolSize = 10
            },
        )

    fun createDsl(dataSource: DataSource): DSLContext = DSL.using(dataSource, SQLDialect.POSTGRES)

    fun migrate(dataSource: DataSource) {
        Flyway
            .configure()
            .dataSource(dataSource)
            .locations("classpath:db/migration")
            .load()
            .migrate()
    }
}
