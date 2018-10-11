package cn.intellif.transaction.intelliftransaction.anotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface TxTransaction {
    //默认超时时间为6秒
    long waitTime()default 6;
}
