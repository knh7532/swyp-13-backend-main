package com.swygbr.backend.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.swygbr.backend.hidden.HiddenData;
import com.swygbr.backend.service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.Map;

@Controller
public class HomeController {


    @GetMapping("/index")
    public String kakaoCallback() {
        return "home/index";
    }

}
