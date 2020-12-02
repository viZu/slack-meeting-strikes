package xyz.ponsold.meetingstrikes.entity

import java.util.*
import javax.persistence.*

@Entity
data class StrikeProposal(
        var teamId: String,
        var channelId: String,
        @ManyToOne
        var strike: Strike,
        var modification: Modification,
        var count: Int,
        var proposerUserId: String,
        var closed: Boolean = false,
        @OneToMany(mappedBy = "proposal", cascade = [CascadeType.ALL])
        var reviewers: MutableList<Reviewer> = ArrayList(),
        @Id
        @GeneratedValue
        var id: Long = 0
)