package generated.model

import kotlin.Int
import kotlin.String

interface SeedsBuilder {
  class DetailedSeed(
    var id: Int?,
    var companyId: String?,
    var seedId: String?,
    var name: String?,
    var maturity: String?,
    var secondaryName: String?,
    var description: String?,
    var image: String?,
    var link: String?
  ) {
    fun build(): Seeds.DetailedSeed = SeedsDto.DetailedSeed(
    id ?: throw IllegalArgumentException("id is not nullable"),
    companyId ?: throw IllegalArgumentException("companyId is not nullable"),
    seedId ?: throw IllegalArgumentException("seedId is not nullable"),
    name ?: throw IllegalArgumentException("name is not nullable"),
    maturity,
    secondaryName,
    description,
    image,
    link
    )}
}
