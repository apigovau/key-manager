
package au.gov.api.registration

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Klaxon
import com.beust.klaxon.Parser
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
import au.gov.api.config.*

import khttp.get
import khttp.structures.authorization.BasicAuthorization

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

    data class Event(var key:String = "", var action:String = "", var type:String = "", var name:String = "", var reason:String = "")

    private fun logEvent(request:HttpServletRequest, action:String, type:String, name:String, reason:String) {
        Thread(Runnable {
            print("Logging Event...")
            // http://www.baeldung.com/get-user-in-spring-security
            val raw = request.getHeader("authorization")
            val logURL = Config.get("BaseRepoURI") + "new"
            if (raw==null) throw RuntimeException()
            val user = String(Base64.getDecoder().decode(raw.removePrefix("Basic "))).split(":")[0]
            val parser: Parser = Parser()
            var eventPayload: JsonObject = parser.parse(StringBuilder(Klaxon().toJsonString(Event(user,action,type,name,reason)))) as JsonObject
            val eventAuth = System.getenv("LogAuthKey")
            val eventAuthUser = eventAuth.split(":")[0]
            val eventAuthPass = eventAuth.split(":")[1]
            var x = khttp.post(logURL,auth=BasicAuthorization(eventAuthUser, eventAuthPass),json = eventPayload)
            println("Log: "+x.statusCode)
        }).start()
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
        if(manager.canWrite(key, "admin") || matchesBootstrapCredentials(key) ){
            val apikey =  manager.newRegistration(email, spaces).apiKey
            try {
                logEvent(request,"Created","Key",apikey,email)
            }
            catch (e:Exception)
            { println(e.message)}

            return apikey
        }
        throw UnauthorisedToCreateKey()
    }

    @GetMapping("/api/delete")
    fun deleteRegistration(request:HttpServletRequest, @RequestParam email:String):String{
        val key = getAPIKeyFromRequest(request)
        if(manager.canWrite(key, "admin")){
            val state = manager.deleteRegistration(email)
            if(state=="Ok"){
                try {
                    logEvent(request,"Deleted","Key",email,"No longer needed")
                }
                catch (e:Exception)
                { println(e.message)}
            }
            return state
        }
        throw UnauthorisedToCreateKey()
    }


    @GetMapping("/api/changeSpaces")
    fun changeSpaces(request:HttpServletRequest, @RequestParam spaces:List<String>, @RequestParam email:String):String{
        val key = getAPIKeyFromRequest(request)
        if(manager.canWrite(key, "admin")){
            manager.updateSpaces( email, spaces)
            try {
                logEvent(request,"Updated","Key",email,"spaces")
            }
            catch (e:Exception)
            { println(e.message)}

            return "OK"
        }
        throw UnauthorisedToCreateKey()
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    class UnauthorisedToCreateKey() : RuntimeException()


    private fun matchesBootstrapCredentials(key:String):Boolean{
        val bootstrapCredentials = System.getenv("BootstrapCredentials")
        if(bootstrapCredentials == null) return false
        if(bootstrapCredentials == "") return false
        return key == bootstrapCredentials
    }

}
