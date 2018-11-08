
package au.gov.api

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.sql.Connection
import java.sql.SQLException
import javax.sql.DataSource



@Component
class RegistrationManager{

        @Value("\${spring.datasource.url}")
        private var dbUrl: String? = null

        @Autowired
        private lateinit var dataSource: DataSource


        fun canWrite(apiKeyText:String, space:String):Boolean{
            val apiKey = APIKey(apiKeyText)
			val registration = getRegistration(apiKey.emailHash)
            return registration.validForSpace(space) && registration.validForAPIKey(apiKey.key)
        }


		fun spacesForKey(apiKey:String):List<String>{
			val keyMD5 = apiKey.split(":")[0]
			val registration = getRegistration(keyMD5)
			return registration.spaces
		}


        private fun createTable(connection:Connection){
            val stmt = connection.createStatement()
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS registrations(registration jsonb)")
        }

		fun newRegistration(email:String, spaces:List<String>):APIKey{
			val registrationAndKey = Registration.new(email, spaces)
			val apiKey = registrationAndKey.apiKey
			val registration = registrationAndKey.registration
			insertRegistration(registration)
			return apiKey
		}

        fun insertRegistration(registration:Registration) {
            var connection: Connection? = null
            try {
                connection = dataSource.connection
				createTable(connection)
                val insertStatement = connection.prepareStatement("INSERT INTO registrations values(?::JSON)")
				insertStatement.setObject(1, registration.toJSON())
				insertStatement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (connection != null) connection.close()
            }
        }



        fun updateSpaces(email:String, spaces:List<String>) {
            val md5 = Registration.md5(email) 
            var connection: Connection? = null
            try {
                connection = dataSource.connection
				createTable(connection)
                val insertStatement = connection.prepareStatement("UPDATE registrations SET registration = jsonb_set(registration, '{spaces}', ?::JSONB , TRUE) WHERE registration->>'emailHash' = ?;")
				insertStatement.setObject(1, Registration.toSpacesArray(spaces))
				insertStatement.setString(2, md5)
				insertStatement.execute()

            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                if (connection != null) connection.close()
            }
        }



        fun getRegistration(md5:String):Registration {
            var connection: Connection? = null
            try {
                connection = dataSource.connection
				createTable(connection)
                val stmt = connection.prepareStatement("SELECT registration from registrations where registration->>'emailHash' = ?")
				stmt.setString(1, md5)
				val rs = stmt.executeQuery()
				while (rs.next()) {
                    val registrationJSON = rs.getString("registration")
					return Registration.parse(registrationJSON)
				}
            } catch (e: Exception) {
                e.printStackTrace()

            } finally {
                if (connection != null) connection.close()
            }
			return Registration("",listOf(),"")
        }


        @Bean
        @Throws(SQLException::class)
        fun dataSource(): DataSource? {
            if (dbUrl?.isEmpty() ?: true) {
                return HikariDataSource()
            } else {
                val config = HikariConfig()
                config.jdbcUrl = dbUrl
                try {
                    return HikariDataSource(config)
                } catch (e: Exception) {
                    return null
                }
            }
        }
}
