package com.hasabati.app.data.db.entities

/** حالة الطلب */
enum class OrderStatus(val arabicLabel: String) {
    CONFIRMED("مؤكد"),
    SHIPPING("قيد الشحن"),
    ARRIVED("وصل"),
    READY_FOR_DELIVERY("جاهز للتسليم"),
    DELIVERED("تم التسليم"),
    CANCELLED("ملغي")
}

/** العملة */
enum class Currency(val arabicLabel: String, val symbol: String) {
    USD("دولار", "$"),
    SYP("ليرة سورية", "ل.س"),
    SAR("ريال سعودي", "ر.س")
}

/** طريقة الدفع / مكان الأموال */
enum class PaymentMethod(val arabicLabel: String) {
    CASH("نقدي"),
    SHAM_CASH("Sham Cash")
}

/**
 * نوع العملية المالية — كل حركة مالية في النظام هي إحدى هذه الأنواع.
 * هذا هو أساس سجل العمليات (Transaction Ledger) الذي تُبنى عليه كل الأرصدة.
 */
enum class TransactionType(val arabicLabel: String) {
    CUSTOMER_PAYMENT("دفعة من عميلة"),
    AGENT_PAYMENT("دفع للوكيلة"),
    EXPENSE("مصروف"),
    PERSONAL_WITHDRAWAL("سحب شخصي"),
    CAPITAL_ADDITION("إضافة رأس مال"),
    CAPITAL_INITIAL("رأس مال ابتدائي"),
    REFUND("استرداد عربون"),
    ADJUSTMENT("تصحيح/تعديل"),
    TRANSFER_OUT("تحويل (سحب)"),
    TRANSFER_IN("تحويل (إيداع)")
}

/** فئات المصاريف المقترحة */
object ExpenseCategories {
    val defaults = listOf("مواصلات", "تغليف", "اتصالات", "توصيل", "عمولة", "مصاريف أخرى")
}
