package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.core.IntellifConnetion;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * 改造数据连接池
 */
@Aspect
@Component
public class DataSourceAspect {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("execution(* *.*..getConnection(..))")
    public Object aroudGetConnetion(ProceedingJoinPoint point) throws Throwable {
        /**
         * 需要开启分布式事务
         */
        if(TransactionConnUtils.getKey()!=null&&!TransactionConnUtils.getKey().equals("")) {
            if(!TransactionConnUtils.canAcessConn()){
                throw new RuntimeException("----------->transaction data source number is run out of ");
            }
            logger.info("-----------> proxy db connection  with key:"+TransactionConnUtils.getKey());
            Object result = point.proceed();
            if(!(result instanceof Connection)){
                return result;
            }
            Connection connection = (Connection) result;
            connection.setAutoCommit(false);
            IntellifConnetion intellifConnetion = new IntellifConnetion(connection);
            TransactionConnUtils.initConn(intellifConnetion);
            TransactionConnUtils.increateConn();
            return intellifConnetion;
        }else{
            return (Connection) point.proceed();
        }
    }
}
