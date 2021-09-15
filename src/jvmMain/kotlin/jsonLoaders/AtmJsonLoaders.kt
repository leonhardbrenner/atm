package jsonLoaders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dbManagers.resourceText
import generated.model.AtmDto
import generated.model.SeedsDto
import javax.inject.Inject

class AtmJsonLoaders @Inject constructor(val kMapper: ObjectMapper) {

    val authorizationPin: List<AtmDto.AuthorizationPin>
        get() = kMapper.readValue(
            resourceText("atm/authorization_pin.json")
        )

    val ledger: List<AtmDto.Ledger>
        get() = kMapper.readValue(
            resourceText("atm/ledger.json")
        )

    val machine: List<AtmDto.Machine>
        get() = kMapper.readValue(
            resourceText("atm/machine.json")
        )

}
