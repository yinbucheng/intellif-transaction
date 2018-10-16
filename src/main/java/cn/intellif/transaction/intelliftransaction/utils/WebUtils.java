package cn.intellif.transaction.intelliftransaction.utils;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class WebUtils {

    private static ServletRequestAttributes getAttributes(){
        return (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    }


    public static HttpServletRequest getRequest(){
        return getAttributes().getRequest();
    }

    public static HttpServletResponse getResponse(){
        return getAttributes().getResponse();
    }
}
