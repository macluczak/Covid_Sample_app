package eu.inkbook.sample.data

data class DataEntity(
    val dateRep: String,
    val day: Int,
    val month: Int,
    val year: Int,
    val cases: Int,
    val deaths: Int,
    val countriesAndTerritories: String,
    val geoId: String,
    val countryterritoryCode: String,
    val popData2019: Long,
    val continentExp: String,
    val Cumulative_number: Double? = 0.0
) {
    companion object{
        const val dir = "/inkbook"
        const val title=  "covid"
        const val extension = ".csv"
    }
}