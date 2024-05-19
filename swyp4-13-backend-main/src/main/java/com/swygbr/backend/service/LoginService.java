package com.swygbr.backend.service;

import java.util.Date;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.swygbr.backend.Repository.AccountRepository;
import com.swygbr.backend.hidden.HiddenData;
import com.swygbr.backend.model.Account;
import com.swygbr.backend.model.RoleType;

import jakarta.servlet.http.HttpServletResponse;

@Service
public class LoginService {

    @Autowired
    private BCryptPasswordEncoder encoder;

    @Autowired
    AccountRepository accountRepository;

    @Transactional
    public void kakaoCallback(Model model, String code) throws JsonProcessingException {
        // POST 방식으로 key=value 데이터를 요청 (카카오쪽으로)
        // 이 때 필요한 라이브러리가 RestTemplate, 얘를 쓰면 http 요청을 편하게 할 수 있다.
        RestTemplate rt = new RestTemplate();

        // HTTP POST를 요청할 때 보내는 데이터(body)를 설명해주는 헤더도 만들어 같이 보내줘야 한다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // body 데이터를 담을 오브젝트인 MultiValueMap를 만들어보자
        // body는 보통 key, value의 쌍으로 이루어지기 때문에 자바에서 제공해주는 MultiValueMap 타입을 사용한다.
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        //params.add("client_id", HiddenData.kakaoRestApi);
        //params.add("redirect_uri", HiddenData.kakaoCallBack);
        params.add("code", code);

        // 요청하기 위해 헤더(Header)와 데이터(Body)를 합친다.
        // kakaoTokenRequest는 데이터(Body)와 헤더(Header)를 Entity가 된다.
        HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

        // POST 방식으로 Http 요청한다. 그리고 response 변수의 응답 받는다.
        ResponseEntity<String> response = rt.exchange(
                "https://kauth.kakao.com/oauth/token", // https://{요청할 서버 주소}
                HttpMethod.POST, // 요청할 방식
                kakaoTokenRequest, // 요청할 때 보낼 데이터
                String.class // 요청 시 반환되는 데이터 타입
        );

        String parsedRespString = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsedResp = objectMapper.readTree(parsedRespString);

        // 액세스 토큰으로 유저를 찾는다.
        String accessToken = parsedResp.get("access_token").asText();
        Account account = findUser(accessToken);

        String refreshToken = parsedResp.get("refresh_token").asText();

        // Model에 토큰 2개를 담아서 응답한다.
        model.addAttribute("accessToken", "Bearer " + accessToken);
        model.addAttribute("refreshToken", "Bearer " + refreshToken);

        // 만료 시간을 밀리초로 변경
        int accessExpire = parsedResp.get("expires_in").asInt() * 1000;
        int refreshExpire = parsedResp.get("refresh_token_expires_in").asInt() * 1000;

        // 토큰의 만료 시간도 담는다.
        model.addAttribute("accessExpire", new Date().getTime() + accessExpire); // 6시간
        model.addAttribute("refreshExpire", new Date().getTime() + refreshExpire); // 2달

        model.addAttribute("accountId", account.getId());
    }

    @Transactional
    public Account findUser(String accessToken) throws JsonProcessingException {
        RestTemplate rt = new RestTemplate();

        // 토큰을 헤더에 담는다.
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

        // 헤더를 가지고 Http 요청 객체를 만든다.
        HttpEntity<MultiValueMap<String, String>> kakaoProfileRequest = new HttpEntity<>(headers);

        // POST 요청을 보낸 후 응답을 받는다.
        ResponseEntity<String> response = rt.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.POST,
                kakaoProfileRequest,
                String.class
        );

        String parsedRespString = response.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode parsedResp = objectMapper.readTree(parsedRespString);

        String id = parsedResp.get("id").asText();
        String username = "K" + id;
        String nickname = parsedResp.get("properties").get("nickname").asText();

        Account account = accountRepository.findByUsername(username);
        if (account == null) { // 가입
            String salt = encoder.encode(UUID.randomUUID().toString());
            String ori_password = salt + UUID.randomUUID().toString();
            String hashed_password = encoder.encode(ori_password);

            account = new Account();
            account.setUsername(username);
            account.setSalt(salt);
            account.setPassword(hashed_password);
            account.setNickname(nickname);
            account.setRole(RoleType.ROLE_USER);

            accountRepository.save(account);
        }

        return account;
    }

    @Transactional
    public int tokenVerify(HttpServletResponse resp, String accessToken, String refreshToken, String accessExpire, String refreshExpire) throws Exception {
        if (!accessToken.startsWith("Bearer ") || accessToken == null) { // 잘못된 액세스 토큰
            return -1;
        }

        Long accessExpireL = Long.parseLong(accessExpire);
        Long refreshExpireL = Long.parseLong(refreshExpire);

        Date now = new Date();
        Date accessExpireDate = new Date(accessExpireL);
        Date refreshExpireDate = new Date(refreshExpireL);

        if (now.after(accessExpireDate)) { // 액세스 토큰 만료
            if (now.before(refreshExpireDate)) { // 리프레시는 유효할 경우
                // 카카오 uri에 요청을 보내 액세스 토큰을 재발급 받는다.
                RestTemplate rt = new RestTemplate();

                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");

                MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
                params.add("grant_type", "refresh_token");
                params.add("client_id", HiddenData.kakaoRestApi);
                params.add("refresh_token", refreshToken.replace("Bearer ", ""));

                HttpEntity<MultiValueMap<String, String>> kakaoTokenRequest = new HttpEntity<>(params, headers);

                ResponseEntity<String> response = rt.exchange(
                        "https://kauth.kakao.com/oauth/token",
                        HttpMethod.POST,
                        kakaoTokenRequest,
                        String.class
                );

                String parsedRespString = response.getBody();
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode parsedResp = objectMapper.readTree(parsedRespString);

                resp.addHeader("ReissuedToken", "Bearer " + parsedResp.get("access_token").asText());
                resp.addHeader("reissuedExpire", Integer.toString(parsedResp.get("expires_in").asInt() * 1000));

                return 2;
            } else { // 리프레시도 만료
                return -1;
            }
        } else { // 액세스 토큰이 유효할 경우
            return 1; // 정상적으로 동작.
        }
    }
}
