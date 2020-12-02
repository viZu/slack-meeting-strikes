package xyz.ponsold.meetingstrikes.entity

import java.util.*
import java.util.function.Predicate
import java.util.stream.Collectors
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.Id
import javax.persistence.OneToMany

@Entity
data class Strike(
        var teamId: String,
        var channelId: String,
        var userId: String,
        var count: Int,
        @OneToMany(mappedBy = "strike")
        var proposals: MutableList<StrikeProposal> = ArrayList(),
        @Id
        @GeneratedValue
        var id: Long = 0
) {
        fun getOpenProposals(): List<StrikeProposal> {
            return proposals.filter { !it.closed }
        }
}