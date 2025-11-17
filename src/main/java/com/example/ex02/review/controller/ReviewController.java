/*ReviewController*/
package com.example.ex02.review.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ReviewController {

    @GetMapping("/designConfig")
    public String designConfigPage() {
        return "review/designConfig";
    }

    @GetMapping("/review")
    public String reviewPage() {
        return "review/review_integration";
    }
}




