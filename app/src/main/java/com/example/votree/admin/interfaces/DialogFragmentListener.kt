interface DialogFragmentListener {
    fun updateExpireBanDateToFirestore(daysToAdd: Int, userId: String)
}