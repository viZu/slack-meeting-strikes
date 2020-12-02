package xyz.ponsold.meetingstrikes

import java.util.regex.Matcher
import java.util.regex.Pattern

val userPattern: Pattern = java.util.regex.Pattern.compile("<@([^|]*)(|.*)?>")

fun String.parseUserId(): String {
    val matcher: Matcher = userPattern.matcher(this)
    return if (matcher.matches()) {
        matcher.group(1)
    } else {
        throw RuntimeException("Aahhh")
    }
}

fun String.toEscapedUserId(): String {
    return "<@${this}>"
}