package jsonLoaders

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import generated.model.AtmDto
import java.io.File
import javax.inject.Inject

fun resource(path: String) = File(ClassLoader.getSystemResource(path).file)
fun resourceText(path: String) = resource(path).readText()

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
