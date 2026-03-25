package com.example.expensetracker.data

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromPriority(priority: Priority?): String? = priority?.name

    @TypeConverter
    fun priorityToString(value: String?): Priority? = value?.let { Priority.valueOf(it) }

    @TypeConverter
    fun fromEmotion(emotion: Emotion?): String? = emotion?.name

    @TypeConverter
    fun emotionToString(value: String?): Emotion? = value?.let { Emotion.valueOf(it) }

    @TypeConverter
    fun fromListString(list: List<String>?): String? = list?.joinToString(",")

    @TypeConverter
    fun stringToListString(value: String?): List<String>? = value?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
}
