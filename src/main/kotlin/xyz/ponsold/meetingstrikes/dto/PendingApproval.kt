package xyz.ponsold.meetingstrikes.dto

import xyz.ponsold.meetingstrikes.entity.Modification
import java.time.Instant

class PendingApproval(val id: Long,
                      val userId: String,
                      val proposerUserId: String,
                      val strikeCount: Int,
                      val modification: Modification,
                      val timestamp: Instant,
                      val approvedBy: List<UserApproval>,
                      val declinedBy: List<UserApproval>) {
}
