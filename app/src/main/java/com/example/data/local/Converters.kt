package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ExpenseCategory
import com.example.data.model.HabitStatus
import com.example.data.model.LogStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.ScheduleType
import com.example.data.model.StressLevel
import com.example.data.model.TrackingType

class Converters {
    @TypeConverter fun fromTrackingType(value: TrackingType): String = value.name
    @TypeConverter fun toTrackingType(value: String): TrackingType = TrackingType.valueOf(value)

    @TypeConverter fun fromScheduleType(value: ScheduleType): String = value.name
    @TypeConverter fun toScheduleType(value: String): ScheduleType = ScheduleType.valueOf(value)

    @TypeConverter fun fromHabitStatus(value: HabitStatus): String = value.name
    @TypeConverter fun toHabitStatus(value: String): HabitStatus = HabitStatus.valueOf(value)

    @TypeConverter fun fromLogStatus(value: LogStatus): String = value.name
    @TypeConverter fun toLogStatus(value: String): LogStatus = LogStatus.valueOf(value)

    @TypeConverter fun fromStressLevel(value: StressLevel): String = value.name
    @TypeConverter fun toStressLevel(value: String): StressLevel = StressLevel.valueOf(value)

    @TypeConverter fun fromPaymentMethod(value: PaymentMethod): String = value.name
    @TypeConverter fun toPaymentMethod(value: String): PaymentMethod = PaymentMethod.valueOf(value)

    @TypeConverter fun fromExpenseCategory(value: ExpenseCategory): String = value.name
    @TypeConverter fun toExpenseCategory(value: String): ExpenseCategory = ExpenseCategory.valueOf(value)
}
