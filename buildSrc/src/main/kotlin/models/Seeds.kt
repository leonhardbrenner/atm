package models

interface Seeds {

    class DetailedSeed(
        val id: Int,
        val companyId: String,
        val seedId: String,
        val name: String,
        val maturity: String?,
        val secondaryName: String?,
        val description: String?,
        val image: String?,
        val link: String?
    )

}
