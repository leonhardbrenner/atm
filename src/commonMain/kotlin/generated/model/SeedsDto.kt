package generated.model

import kotlin.Int
import kotlin.String
import kotlinx.serialization.Serializable

interface SeedsDto {
  @Serializable
  data class DetailedSeed(
    override val id: Int,
    override val companyId: String,
    override val seedId: String,
    override val name: String,
    override val maturity: String?,
    override val secondaryName: String?,
    override val description: String?,
    override val image: String?,
    override val link: String?
  ) : Seeds.DetailedSeed {
    companion object {
      const val path: String = "/Seeds/DetailedSeed"

      fun create(source: Seeds.DetailedSeed) = SeedsDto.DetailedSeed(source.id, source.companyId,
          source.seedId, source.name, source.maturity, source.secondaryName, source.description,
          source.image, source.link)}
  }
}
