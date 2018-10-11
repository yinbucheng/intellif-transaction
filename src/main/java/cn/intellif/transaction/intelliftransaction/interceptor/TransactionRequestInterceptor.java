package cn.intellif.transaction.intelliftransaction.interceptor;

import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

@Component
public class TransactionRequestInterceptor implements RequestInterceptor {
    @Override
    public void apply(RequestTemplate template) {
        String key = TransactionConnUtils.getKey();
        if(key!=null){
            template.header(Constant.TRANSATION_TOKEN,key);
        }
        Enumeration<String> headerNames = WebUtils.getRequest().getHeaderNames();
        if(headerNames!=null){
            while(headerNames.hasMoreElements()){
                String name = headerNames.nextElement();
                String value = WebUtils.getRequest().getHeader(name);
                template.header(name,value);
            }
        }
    }
}
