package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.anotation.TxTransaction;
import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.LockUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;

@Aspect
@Component
public class TransactionAspect implements Ordered {

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(org.springframework.transaction.annotation.Transactional)")
    public Object aroundWithTr(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTransaction(joinPoint);
    }

    @Around("this(org.springframework.transaction.annotation.Transactional) && execution( * *(..))")
    public Object aroundWithTR(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTransaction(joinPoint);
    }

    private Object runTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        Class clazz =  AopUtils.getTargetClass(joinPoint.getTarget());
        if(clazz.getAnnotation(TxTransaction.class)!=null&& TransactionConnUtils.keyIsNotEmpty()){
            return joinPoint.proceed();
        }
        String token =   WebUtils.getRequest().getHeader(Constant.TRANSATION_TOKEN);
        logger.info("----------->acquire current transaction token:"+token);
        if(token==null){
            return joinPoint.proceed();
        }
        TransactionConnUtils.initKey(token);
        try{
            if(!SocketManager.getInstance().getNetState()){
                throw new RuntimeException("-----------> txmanger is closed please make sure txmanager is running");
            }
            logger.info("----------->in transaction proxy method");
            LockUtils.initLock(token);
            //将唯一标示告诉txManager
            SocketManager.getInstance().sendMsg(ProtocolUtils.register());
            LockUtils.getLock(token).await(60);
            return joinPoint.proceed();
        }catch (Exception e){
            //发送异常信息告诉txManager
            LockUtils.initLock(token);
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            LockUtils.getLock(token).await(60);
            throw new RuntimeException(e);
        }finally {
            TransactionConnUtils.release();
            LockUtils.removeLock(token);
        }

    }

    /**
     * 优先级要低于TxTransactionAspect
     * @return
     */
    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE+1;
    }
}

