package mqtt

data class Broker(val name: String, val host: String, val port: Int, val protocol: Protocol) {

    private val mPublications = mutableListOf<Publication>()

    fun addPublication(publication: Publication) {
        mPublications.add(publication)
    }

    fun removePublication(publication: Publication) {
        mPublications.remove(publication)
    }

    fun getUri(): String {
        return "${protocol.str}://$host:$port"
    }
}