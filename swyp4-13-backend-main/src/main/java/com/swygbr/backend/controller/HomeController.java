package com.kimdev.SubwayNotify.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.kimdev.SubwayNotify.hidden.HiddenData;
import com.kimdev.SubwayNotify.service.HomeService;
import com.kimdev.SubwayNotify.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {

    @Autowired
    HomeService homeService;

    @GetMapping("/index")
    public String kakaoCallback() {
        return "home/index";
    }

    @GetMapping("/setalarm")
    public String setalarm() {

        return "alarm/step1";
    }

    @GetMapping("/goSubwayStep")
    public String goSubwayStep(Model model, @RequestParam int subwayId) {
        String s_Name = homeService.findSubwayWithId(subwayId);
        model.addAttribute("s_Id", subwayId);
        model.addAttribute("s_Name", s_Name);
        return "alarm/setAlarm";
    }

    @GetMapping("/searchAlarms")
    public String searchAlarms() {
        return "home/searchAlarms";
    }

    @GetMapping("/faq")
    public String faq() {
        return "home/faq";
    }
}
