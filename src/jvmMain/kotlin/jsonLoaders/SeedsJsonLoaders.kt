package jsonLoaders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dbManagers.resourceText
import generated.model.SeedsDto
import javax.inject.Inject

class SeedsJsonLoaders @Inject constructor(val kMapper: ObjectMapper) {

    val detailedSeeds: List<SeedsDto.DetailedSeed>
        get() = kMapper.readValue(
            resourceText("seeds/detailed-seeds.json")
        )

}
