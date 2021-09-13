package services.seeds

import generated.dao.SeedsDao
import generated.model.Seeds
import javax.inject.Inject

class DetailedSeedService @Inject constructor(val dao: SeedsDao.DetailedSeed) {

    fun index() = dao.index()

    fun create(source: Seeds.DetailedSeed) = dao.create(source)

    fun update(source: Seeds.DetailedSeed) = dao.update(source)

    fun destroy(id: Int) = dao.destroy(id)

}
