
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
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import javax.servlet.http.HttpServletRequest
import java.util.Base64

@RestController
class APIController {

    @Autowired
    private lateinit var manager: RegistrationManager

    private fun getAPIKeyFromRequest(request:HttpServletRequest):String{
        // http://www.baeldung.com/get-user-in-spring-security
        val raw = request.getHeader("authorization")
        val apikey = String(Base64.getDecoder().decode(raw.removePrefix("Basic ")))
        return apikey
    }


    @GetMapping("/api/canWrite")
    fun canWrite(request:HttpServletRequest, @RequestParam space:String):Boolean{
        val key = getAPIKeyFromRequest(request)
        return manager.canWrite(key, space)
    }

    @GetMapping("/api/spaces")
    fun spaces(request:HttpServletRequest):List<String>{
        val key = getAPIKeyFromRequest(request)
        return manager.spacesForKey(key)
    }

    @GetMapping("/api/new")
    fun newRegistration(request:HttpServletRequest, @RequestParam email:String, @RequestParam spaces:List<String>):String{
        val key = getAPIKeyFromRequest(request)
        if(manager.canWrite(key, "admin")){
            return manager.newRegistration(email, spaces).apiKey
        }
        throw UnauthorisedToCreateKey()
    }


    @GetMapping("/api/changeSpaces")
    fun changeSpaces(request:HttpServletRequest, @RequestParam spaces:List<String>, @RequestParam email:String):String{
        val key = getAPIKeyFromRequest(request)
        if(manager.canWrite(key, "admin")){
            manager.updateSpaces( email, spaces)
            return "OK"
        }
        throw UnauthorisedToCreateKey()
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    class UnauthorisedToCreateKey() : RuntimeException()

}
