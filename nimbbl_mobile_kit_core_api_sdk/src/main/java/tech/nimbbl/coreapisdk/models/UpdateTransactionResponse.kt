package tech.nimbbl.coreapisdk.models

data class UpdateTransactionResponse(
    val additional_charges: Double,
    val bank_name: String,
    val expiry: String,
    val freecharge_payment_mode: String,
    val grand_total_amount: Double,
    val holder_name: String,
    val id: Int,
    val issuer: String,
    val masked_card: String,
    val merchant_callback_url: String,
    val merchant_id: Int,
    val network: String,
    val nimbbl_consumer_message: String,
    val nimbbl_error_code: String,
    val nimbbl_merchant_message: String,
    val order_id: Int,
    val orignal_payment_transaction_id: String,
    val payment_mode: String,
    val payment_partner: String,
    val paytm_payment_mode: String,
    val psp_generated_redirect: String,
    val psp_generated_redirect_type: String,
    val psp_generated_txn_id: String,
    val refund_amount: String,
    val refund_arn: String,
    val refund_done: String,
    val retry_allowed: String,
    val status: String,
    val sub_merchant_id: Int,
    val transaction_id: String,
    val transaction_status_monitoring_done: Boolean,
    val transaction_status_monitoring_done_at: String,
    val transaction_status_monitoring_status_change_detected: String,
    val transaction_type: String,
    val user_id: Int,
    val vpa_app_name: String,
    val vpa_holder: String,
    val vpa_id: String,
    val wallet_name: String,
    val webhook_sent: Boolean
)