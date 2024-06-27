package tech.nimbbl.exmaple.model.createorder

data class User(
    val country_code: String,
    val email: String,
    val first_name: String,
    val last_name: String,
    val mobile_number: Long,
    val user_id: String
)