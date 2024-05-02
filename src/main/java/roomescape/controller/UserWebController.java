package roomescape.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserWebController {

    @GetMapping
    public String getPopularPage() {
        return "/index";
    }

    @GetMapping("/reservation")
    public String getUserReservationPage() {
        return "/reservation";
    }
}