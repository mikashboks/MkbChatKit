/*******************************************************************************
 * Copyright 2016 stfalcon.com
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stfalcon.chatkit.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class DateFormatter private constructor() {
    init {
        throw AssertionError()
    }

    /**
     * Interface used to format dates before they were displayed (e.g. dialogs time, messages date headers etc.).
     */
    interface Formatter {
        /**
         * Formats an string representation of the date object.
         *
         * @param date The date that should be formatted.
         * @return Formatted text.
         */
        fun format(date: Date?): String?
    }

    enum class Template(private val template: String) {
        STRING_DAY_MONTH_YEAR("d MMMM yyyy"), STRING_DAY_MONTH("d MMMM"), TIME("HH:mm");

        fun get(): String {
            return template
        }
    }

    companion object {
        fun format(date: Date?, template: Template): String {
            return format(date, template.get())
        }

        @JvmStatic
        fun format(date: Date?, format: String?): String {
            return if (date == null) "" else SimpleDateFormat(format, Locale.getDefault())
                .format(date)
        }

        fun isSameDay(date1: Date?, date2: Date?): Boolean {
            require(!(date1 == null || date2 == null)) { "Dates must not be null" }
            val cal1 = Calendar.getInstance()
            cal1.time = date1
            val cal2 = Calendar.getInstance()
            cal2.time = date2
            return isSameDay(cal1, cal2)
        }

        fun isSameDay(cal1: Calendar?, cal2: Calendar?): Boolean {
            require(!(cal1 == null || cal2 == null)) { "Dates must not be null" }
            return cal1[Calendar.ERA] == cal2[Calendar.ERA] && cal1[Calendar.YEAR] == cal2[Calendar.YEAR] && cal1[Calendar.DAY_OF_YEAR] == cal2[Calendar.DAY_OF_YEAR]
        }

        fun isSameYear(date1: Date?, date2: Date?): Boolean {
            require(!(date1 == null || date2 == null)) { "Dates must not be null" }
            val cal1 = Calendar.getInstance()
            cal1.time = date1
            val cal2 = Calendar.getInstance()
            cal2.time = date2
            return isSameYear(cal1, cal2)
        }

        fun isSameYear(cal1: Calendar?, cal2: Calendar?): Boolean {
            require(!(cal1 == null || cal2 == null)) { "Dates must not be null" }
            return cal1[Calendar.ERA] == cal2[Calendar.ERA] &&
                cal1[Calendar.YEAR] == cal2[Calendar.YEAR]
        }

        fun isToday(calendar: Calendar?): Boolean {
            return isSameDay(calendar, Calendar.getInstance())
        }

        fun isToday(date: Date?): Boolean {
            return isSameDay(date, Calendar.getInstance().time)
        }

        fun isYesterday(calendar: Calendar?): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_MONTH, -1)
            return isSameDay(calendar, yesterday)
        }

        fun isYesterday(date: Date?): Boolean {
            val yesterday = Calendar.getInstance()
            yesterday.add(Calendar.DAY_OF_MONTH, -1)
            return isSameDay(date, yesterday.time)
        }

        fun isCurrentYear(date: Date?): Boolean {
            return isSameYear(date, Calendar.getInstance().time)
        }

        fun isCurrentYear(calendar: Calendar?): Boolean {
            return isSameYear(calendar, Calendar.getInstance())
        }

        fun isBeforeDay(date1: Date, date2: Date): Boolean {
            val cal1: Calendar = Calendar.getInstance()
            cal1.time = date1
            val cal2: Calendar = Calendar.getInstance()
            cal2.time = date2
            return isBeforeDay(cal1, cal2)
        }

        /**
         *
         *
         * Checks if the first calendar date is before the second calendar date ignoring time.
         *
         *
         * @param cal1 the first calendar, not altered, not null.
         * @param cal2 the second calendar, not altered, not null.
         * @return true if cal1 date is before cal2 date ignoring time.
         * @throws IllegalArgumentException if either of the calendars are `null`
         */
        fun isBeforeDay(cal1: Calendar, cal2: Calendar): Boolean {
            if (cal1.get(Calendar.ERA) < cal2.get(Calendar.ERA)) return true
            if (cal1.get(Calendar.ERA) > cal2.get(Calendar.ERA)) return false
            if (cal1.get(Calendar.YEAR) < cal2.get(Calendar.YEAR)) return true
            return if (cal1.get(Calendar.YEAR) > cal2.get(Calendar.YEAR)) false
            else cal1.get(Calendar.DAY_OF_YEAR) < cal2.get(Calendar.DAY_OF_YEAR)
        }

    }
}
