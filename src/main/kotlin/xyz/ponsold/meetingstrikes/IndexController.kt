package xyz.ponsold.meetingstrikes

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseBody

@Controller
class IndexController {

    @RequestMapping("/")
    @ResponseBody
    fun home(): String {
        return "Hello after Github Push meeting-strikes"
    }
}