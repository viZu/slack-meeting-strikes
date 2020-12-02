package xyz.ponsold.meetingstrikes.entity

import java.time.Instant
import javax.persistence.*

@Entity
data class Reviewer(
        var userId: String,
        var outcome: ReviewOutcome,
        @ManyToOne(cascade = [CascadeType.ALL])
        var proposal: StrikeProposal,
        var timestamp: Instant = Instant.now(),
        @Id
        @GeneratedValue
        var id: Long = 0
)