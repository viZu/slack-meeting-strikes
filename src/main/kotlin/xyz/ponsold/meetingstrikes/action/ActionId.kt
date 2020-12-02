package xyz.ponsold.meetingstrikes.action

import java.util.regex.Pattern

enum class ActionId(private val idPrefix: String) {
    NEW_STRIKE_APPROVED("new-strike-approved-"),
    NEW_STRIKE_DECLINED("new-strike-declined-"),
    REVIEWS_APPROVE("review-approved-"),
    REVIEWS_DECLINE("review-declined-"),
    LIST_OVERFLOW_ACTIONS("list-overflow-actions"),
    LIST_OVERFLOW_ACTIONS_ADD("list-overflow-actions-add-"),
    LIST_OVERFLOW_ACTIONS_REMOVE("list-overflow-actions-remove-");

    private val pattern: Pattern = Pattern.compile("$idPrefix.*")

    fun idPrefix(): String {
        return idPrefix
    }

    fun id(identifier: Any): String {
        return idPrefix + identifier.toString()
    }

    fun pattern(): Pattern {
        return pattern
    }

    fun matches(actionId: String): Boolean {
        return pattern.matcher(actionId).matches()
    }

    fun getIdentifier(actionId: String): Long {
        return getStringIdentifier(actionId).toLong()
    }

    fun getStringIdentifier(actionId: String): String {
        return actionId.substring(idPrefix.length)
    }

}