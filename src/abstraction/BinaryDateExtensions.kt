package abstraction

fun String.parseDay(fsConstants: FsConstants): Int = substring(fsConstants.DATE_DAY_START, fsConstants.DATE_DAY_END).toInt(2)

fun String.parseMonth(fsConstants: FsConstants): Int = substring(fsConstants.DATE_DAY_END, fsConstants.DATE_MONTH_END).toInt(2)

fun String.parseYear(fsConstants: FsConstants): Int =
    substring(fsConstants.DATE_MONTH_END, fsConstants.DATE_YEAR_END).toInt(2) + fsConstants.BASE_YEAR

fun String.parseHour(fsConstants: FsConstants): Int = substring(fsConstants.DATE_YEAR_END, fsConstants.DATE_HOUR_END).toInt(2)

fun String.parseMinute(fsConstants: FsConstants): Int = substring(fsConstants.DATE_HOUR_END, fsConstants.DATE_MINUTE_END).toInt(2)

fun String.parseSecond(fsConstants: FsConstants): Int = substring(fsConstants.DATE_MINUTE_END, fsConstants.DATE_SECOND_END).toInt(2)

fun String.toDateString(fsConstants: FsConstants): String =
    "${parseDay(fsConstants)}/${parseMonth(fsConstants)}/${parseYear(fsConstants)} " +
        "${parseHour(fsConstants)}:${parseMinute(fsConstants)}:${parseSecond(fsConstants)}"
