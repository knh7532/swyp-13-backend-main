package com.swygbr.backend.hidden;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class HiddenData {
    private String kakaoRestApi;
    private String kakaoCallBack;
    
    public String kakaoRestApi(){
          return "client-id";
    }
    
    public String kakaoCallBack(){
        return "redirect-uri";    
}
};