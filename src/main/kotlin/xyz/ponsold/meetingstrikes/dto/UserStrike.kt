package xyz.ponsold.meetingstrikes.dto

data class UserStrike(val id: Long,
                      val userId: String,
                      val strikeCount: Int,
                      val pendingCountAdd: Int,
                      val pendingCountRemove: Int)