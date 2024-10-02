package com.springboot.utils;

import com.springboot.exception.BusinessLogicException;
import com.springboot.exception.ExceptionCode;
import org.springframework.security.core.Authentication;

import java.util.Map;
import java.util.Optional;

public class CredentialUtil {
    private static Map<String, String> getCredentialsAsMap(Authentication authentication){
        return (Map<String, String>) (authentication.getCredentials());
    }

    public static String getCredentialField(Authentication authentication, String key){
        Map<String, String> credentials = getCredentialsAsMap(authentication);
        if(credentials.containsKey(key)){
            return credentials.get(key);
        }else {
            throw new BusinessLogicException(ExceptionCode.CREDENTIAL_NOT_FOUND);
        }
    }
}
