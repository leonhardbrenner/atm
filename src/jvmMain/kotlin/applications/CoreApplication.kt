package applications

//import applications.routing.*
import generated.routing.SeedsRouting
import com.google.inject.AbstractModule
import javax.inject.Inject
import io.ktor.routing.*
import generated.dao.SeedsDao
import services.SeedsService

class CoreApplication @Inject constructor(
    val dao: SeedsDao,
    val detailedSeed: SeedsRouting.DetailedSeed,
    ) {

    fun routesFrom(routing: Routing) {
        detailedSeed.routes(routing)
    }

    object Module : AbstractModule() {

        override fun configure() {
            //Stonesoup PR 10 removed the rest database and all MongoDB wiring.
            //bind(CoroutineDatabase::class.java).toInstance(database())
        }

    }

}
