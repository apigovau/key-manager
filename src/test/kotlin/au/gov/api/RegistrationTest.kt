package au.gov.api

import org.junit.Assert
import org.junit.Test

class RegistrationTest{

    @Test
    fun can_deserialise_registration(){
        val keyJson = """{"email":"user@test.gov.au","spaces":["space1","space2"],"hash":"a:registration"}"""
		val registration = Registration.parse(keyJson)

		Assert.assertEquals("user@test.gov.au", registration.email)
		Assert.assertEquals(listOf("space1","space2"), registration.spaces)
		Assert.assertEquals("a:registration", registration.hash)
	}


	@Test 
	fun can_generate_new_registration(){
		val email = "a@test.gov.au"
		val spaces = listOf<String>()
		val registrationAndKey = Registration.new(email, spaces)
		val registration = registrationAndKey.registration
	
		Assert.assertEquals(email, registration.email)
		Assert.assertEquals(spaces, registration.spaces)
	}



	@Test
	fun is_key_valid_for_space(){
        println("Some standard out")
		val email = "a@test.gov.au"
		val spaces = listOf("space1","space2")
		val registraiton = Registration.new(email, spaces).registration

		Assert.assertTrue(registraiton.validForSpace("space1"))
		Assert.assertTrue(registraiton.validForSpace("space2"))
		Assert.assertFalse(registraiton.validForSpace("space3"))
	}

	@Test
	fun can_check_hashes(){

		val email = "a@test.gov.au"
		val spaces = listOf<String>()
		val registrationAndKey = Registration.new(email, spaces)
		val registrationAndKey2 = Registration.new(email, spaces)
	
		Assert.assertTrue(registrationAndKey.registration.validForAPIKey(registrationAndKey.apiKey.key))
		Assert.assertFalse(registrationAndKey.registration.validForAPIKey(registrationAndKey2.apiKey.key))
	}


    @Test
    fun can_serialise_spaces_array(){

        val l = listOf("space1","space2","space3")
        val j = """["space1", "space2", "space3"]"""
        Assert.assertEquals(Registration.toSpacesArray(l),j)
    }
}
