package com.swygbr.backend.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.swygbr.backend.service.LoginService;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class LoginApiController {

    @Autowired
    LoginService loginService;

    @GetMapping("/tokenVerify")
    public int tokenVerify(@RequestHeader("Authorization") String accessToken,
                           @RequestHeader("Refresh-Token") String refreshToken,
                           @RequestHeader("accessExpire") String accessExpire,
                           @RequestHeader("refreshExpire") String refreshExpire,
                           HttpServletResponse response) throws Exception {
        int result = loginService.tokenVerify(response, accessToken, refreshToken, accessExpire, refreshExpire);

        return result;
    }

}
