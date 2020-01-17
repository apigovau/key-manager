package au.gov.api.registration


class APIKey() {
    var emailHash: String = ""
    var key: String = ""
    var apiKey: String = ""

    constructor(email: String, theKey: String) : this() {
        emailHash = email
        key = theKey
        apiKey = "${emailHash}:${key}"
    }

    constructor(theApiKey: String) : this() {
        val parts = theApiKey.split(":")
        emailHash = parts[0]
        key = parts[1]
        apiKey = theApiKey
    }


}
