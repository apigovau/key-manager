package au.gov.api

import com.beust.klaxon.Klaxon
import java.util.UUID
import org.mindrot.jbcrypt.BCrypt
import java.math.BigInteger
import java.security.MessageDigest


class APIKey(){
    var emailHash:String = ""
    var key:String = ""
    var apiKey:String = ""

    constructor(email:String, theKey:String) : this(){
        emailHash = email
        key = theKey
        apiKey = "${emailHash}:${key}"
    }

    constructor(theApiKey:String) : this(){
        val parts = theApiKey.split(":")
        emailHash = parts[0]
        key = parts[1]
        apiKey = theApiKey
    }


}
