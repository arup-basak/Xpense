package models

class TransactionModel(
    var id: Int,
    var transactionName: String,
    var transactionAmount: Int,
    var timeLong: Long,
    var transactionNote: String
) {
    override fun toString(): String {
        return "$transactionName $transactionAmount $timeLong $transactionNote"
    }
}
