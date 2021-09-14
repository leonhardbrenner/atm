package applications

import com.google.inject.AbstractModule
import javax.inject.Inject
import io.ktor.routing.*

class CoreApplication @Inject constructor(
    //val detailedSeed: SeedsRouting.DetailedSeed,
    ) {

    fun routesFrom(routing: Routing) {
        //detailedSeed.routes(routing)
    }

    object Module : AbstractModule() {

        override fun configure() {
            //Stonesoup PR 10 removed the rest database and all MongoDB wiring.
            //bind(CoroutineDatabase::class.java).toInstance(database())
        }

    }

}
