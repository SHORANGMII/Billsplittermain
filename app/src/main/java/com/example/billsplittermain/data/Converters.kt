package com.example.billsplittermain.data

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters to allow Room to reference complex data types.
 * Room can only store primitive types, so we need to convert types like [Date]
 * into [Long] timestamps for database storage.
 */
class Converters {
    /**
     * Converts a [Date] object into a [Long] timestamp in milliseconds for storage.
     */
    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts a [Long] timestamp back into a [Date] object.
     */
    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }
}
