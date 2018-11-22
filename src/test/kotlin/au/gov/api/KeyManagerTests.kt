package au.gov.api


import org.junit.Assert
import org.junit.Test

import javax.sql.DataSource
import java.sql.Connection
import java.sql.SQLException

import com.opentable.db.postgres.embedded.EmbeddedPostgres

class MetaDataTest{

    var manager = RegistrationManager(MockDataSource())

	@Test
    fun can_round_trip_registration(){

		val email = "a@a.com"
		val spaces = listOf("space1","space2")
		val registrationKey = manager.newRegistration(email, spaces)
 
 		val newSpaces = manager.spacesForKey(registrationKey.apiKey)

		Assert.assertEquals(spaces, newSpaces)
		val registration = manager.getRegistration(Registration.md5(email))!!

		Assert.assertEquals(email, registration.email)
		Assert.assertEquals(spaces, registration.spaces)

    }

	

	@Test
    fun can_delete_keys(){

		val email = "a@a.com"
		val spaces = listOf("space1","space2")
		val registrationKey = manager.newRegistration(email, spaces)

		manager.deleteRegistration(email)

		val registration = manager.getRegistration(Registration.md5(email))
		Assert.assertNull(registration)

    }

}
