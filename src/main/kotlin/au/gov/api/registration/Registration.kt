package au.gov.api.registration

import com.beust.klaxon.Klaxon
import org.mindrot.jbcrypt.BCrypt
import java.math.BigInteger
import java.security.MessageDigest
import java.util.*

data class RegistrationAndKey(val registration: Registration, val apiKey: APIKey)

class Registration(val email: String, val spaces: List<String>, val hash: String) {
    val emailHash: String

    init {
        emailHash = md5(email)
    }


    fun validForSpace(space: String) = (space in spaces) || ("admin" in spaces)

    fun validForAPIKey(apiKey: String): Boolean {
        return BCrypt.checkpw(apiKey, hash)
    }

    fun toJSON() = Klaxon().toJsonString(this)


    override fun toString() = "Registration(${email}):"

    companion object {
        fun parse(json: String): Registration {
            return Klaxon().parse<Registration>(json)!!
        }

        fun new(email: String, spaces: List<String>): RegistrationAndKey {
            val keyMatter = UUID.randomUUID().toString()
            val newHash = hashKey(keyMatter)
            val key = Registration(email, spaces, newHash)
            return RegistrationAndKey(key, APIKey(md5(email), keyMatter))
        }

        fun hashKey(apiKey: String): String {
            val hash = BCrypt.hashpw(apiKey, BCrypt.gensalt())
            return hash
        }


        fun md5(thing: String): String {
            val md = MessageDigest.getInstance("MD5")
            md.update(thing.toByteArray(), 0, thing.length)
            return BigInteger(1, md.digest()).toString(16).toString()
        }

        fun toSpacesArray(spaces: List<String>): String = Klaxon().toJsonString(spaces)

    }

}
