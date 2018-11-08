
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

@RestController
class APIController {

    @Autowired
    private lateinit var manager: RegistrationManager

    @GetMapping("/api/canWrite")
    fun canWrite(@RequestParam key:String, @RequestParam space:String):Boolean{
        return manager.canWrite(key, space)
    }

    @GetMapping("/api/spaces")
    fun spaces(@RequestParam key:String):List<String>{
        return manager.spacesForKey(key)
    }

    @GetMapping("/api/new")
    fun newRegistration(@RequestParam email:String, @RequestParam spaces:List<String>, @RequestParam key:String):String{
        if(manager.canWrite(key, "admin")){
            return manager.newRegistration(email, spaces).apiKey
        }
        throw UnauthorisedToCreateKey()
    }


    @GetMapping("/api/changeSpaces")
    fun changeSpaces(@RequestParam spaces:List<String>, @RequestParam email:String, @RequestParam key:String):String{
        if(manager.canWrite(key, "admin")){
            manager.updateSpaces( email, spaces)
            return "OK"
        }
        throw UnauthorisedToCreateKey()
    }


    @ResponseStatus(HttpStatus.FORBIDDEN)
    class UnauthorisedToCreateKey() : RuntimeException()

}
