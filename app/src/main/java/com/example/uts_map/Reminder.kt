package com.example.uts_map

class Reminder(
    val time: String,
    val daysOfWeek: BooleanArray,
    var isActive: Boolean = true
) {
    fun copy(
        time: String = this.time,
        daysOfWeek: BooleanArray = this.daysOfWeek.copyOf(),
        isActive: Boolean = this.isActive
    ): Reminder {
        return Reminder(time, daysOfWeek, isActive)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Reminder

        if (time != other.time) return false
        if (!daysOfWeek.contentEquals(other.daysOfWeek)) return false
        if (isActive != other.isActive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = time.hashCode()
        result = 31 * result + daysOfWeek.contentHashCode()
        result = 31 * result + isActive.hashCode()
        return result
    }

    companion object {
        // Utility function untuk membuat array hari kosong
        fun createEmptyDaysArray(): BooleanArray {
            return BooleanArray(7) { false }
        }
    }
}