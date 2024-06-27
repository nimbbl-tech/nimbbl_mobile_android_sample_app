package tech.nimbbl.exmaple.model.createorder

data class CreateOrderResponse(
    val amount_before_tax: Int,
    val attempts: Int,
    val currency: String,
    val currency_conversion: CurrencyConversion,
    val invoice_id: String,
    val next: List<Next>,
    val order_date: String,
    val order_id: String,
    val refresh_token: String,
    val refresh_token_expiration: String,
    val status: String,
    val tax: Int,
    val token: String,
    val token_expiration: String,
    val total_amount: Int,
    val user: User
)