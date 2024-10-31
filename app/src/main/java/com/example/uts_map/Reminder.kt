package com.example.uts_map

data class Reminder(
    val time: String,
    val daysOfWeek: BooleanArray,
    var isEnabled: Boolean
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reminder

        if (time != other.time) return false
        if (!daysOfWeek.contentEquals(other.daysOfWeek)) return false
        if (isEnabled != other.isEnabled) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + daysOfWeek.contentHashCode()
        result = 31 * result + isEnabled.hashCode()
        return result
    }
}