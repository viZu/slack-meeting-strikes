package xyz.ponsold.meetingstrikes.config

import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import java.lang.IllegalStateException
import java.util.regex.Pattern
import javax.sql.DataSource


@Configuration
@Profile("prod")
class DataSourceConfiguration {

    private val databasePattern = Pattern.compile(".*//(.*):(.*)@(.*)")

    @Bean
    fun dataSource(environment: Environment): DataSource {
        val dataBaseUrl = environment.getProperty("DATABASE_URL") ?: ""
        val matcher = databasePattern.matcher(dataBaseUrl)
        if (!matcher.matches()) {
            throw IllegalStateException("Database url not correctly configured")
        }

        val username = matcher.group(1)
        val password = matcher.group(2)
        val url = matcher.group(3)

        val dataSourceBuilder = DataSourceBuilder.create()
        dataSourceBuilder.driverClassName("org.postgresql.Driver")
        dataSourceBuilder.url("jdbc:postgresql://$url")
        dataSourceBuilder.username(username)
        dataSourceBuilder.password(password)
        return dataSourceBuilder.build()
    }
}