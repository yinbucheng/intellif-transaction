package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.anotation.TxTransaction;
import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TxTransactionAspect  implements Ordered {

    @Value("${intellif.transacton.timeout}")
    private Integer timeout;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction)")
    public Object aroundWithTx(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    @Around("this(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction) && execution( * *(..))")
    public Object aroundWithTx2(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    private Object runTxTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        //判断其是否为上一个调用链过来的如果是直接放行
        String token =   WebUtils.getRequest().getHeader(Constant.TRANSATION_TOKEN);
        if(token!=null&&!token.equals("")){
            return joinPoint.proceed();
        }
        /**
         * 创建唯一标示
         */
        TransactionConnUtils.intKey();
        try{
            if(!SocketManager.getInstance().getNetState()){
                throw new RuntimeException("----> txmanger is closed please make sure txmanager is running");
            }
            //将唯一表示告诉txmanger并开启超时机制
            SocketManager.getInstance().sendMsg(ProtocolUtils.register());
            Object result =  joinPoint.proceed();
           //发送提交命令 及关闭命令
            SocketManager.getInstance().sendMsg(ProtocolUtils.commit());
            return result;
        }catch (Exception e){
            //发送回滚及 关闭命令
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            throw new RuntimeException(e);
        }finally {
            //释放资源
            Thread.sleep(100);
            SocketManager.getInstance().sendMsg(ProtocolUtils.clear());
            TransactionConnUtils.release();
        }
    }


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
        if(clazz.getAnnotation(TxTransaction.class)!=null&&TransactionConnUtils.keyIsNotEmpty()){
            return joinPoint.proceed();
        }
        String token =   WebUtils.getRequest().getHeader(Constant.TRANSATION_TOKEN);
        logger.info("------------------->acquire current transaction token:"+token);
        if(token==null){
            return joinPoint.proceed();
        }
        TransactionConnUtils.initKey(token);
        try{
            if(!SocketManager.getInstance().getNetState()){
                throw new RuntimeException("----> txmanger is closed please make sure txmanager is running");
            }
            logger.info("---------------------->in transaction proxy method");
            //将唯一标示告诉txManager
            SocketManager.getInstance().sendMsg(ProtocolUtils.register());
            return joinPoint.proceed();
        }catch (Exception e){
            //发送异常信息告诉txManager
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            throw new RuntimeException(e);
        }finally {
            TransactionConnUtils.release();
        }

    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
