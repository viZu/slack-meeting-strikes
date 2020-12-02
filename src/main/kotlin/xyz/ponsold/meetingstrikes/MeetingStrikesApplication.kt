package xyz.ponsold.meetingstrikes

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.boot.web.servlet.ServletComponentScan

@SpringBootApplication
@ServletComponentScan
class MeetingStrikesApplication

fun main(args: Array<String>) {
    runApplication<MeetingStrikesApplication>(*args)
}
