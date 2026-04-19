package com.yourapp.premium.utils

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourapp.premium.data.models.BillingCycle
import com.yourapp.premium.data.models.PurchaseState

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(value: List<String>?): String? {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toStringList(value: String?): List<String>? {
        return if (value == null) {
            null
        } else {
            val listType = object : TypeToken<List<String>>() {}.type
            gson.fromJson(value, listType)
        }
    }

    @TypeConverter
    fun fromBillingCycle(value: BillingCycle?): String? {
        return value?.name
    }

    @TypeConverter
    fun toBillingCycle(value: String?): BillingCycle? {
        return value?.let { BillingCycle.valueOf(it) }
    }

    @TypeConverter
    fun fromPurchaseState(value: PurchaseState?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPurchaseState(value: String?): PurchaseState? {
        return value?.let { PurchaseState.valueOf(it) }
    }
}

