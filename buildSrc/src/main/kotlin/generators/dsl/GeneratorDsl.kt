package generators.dsl

import schema.Manifest
import schema.ManifestDsl

interface GeneratorDsl {

    fun generate(namespace: ManifestDsl.Namespace)

    val path
        get() = "/home/lbrenner/projects/stonesoup/src" //Todo - move to config

}

