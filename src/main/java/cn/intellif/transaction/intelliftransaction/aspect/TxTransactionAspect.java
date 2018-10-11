package cn.intellif.transaction.intelliftransaction.aspect;

import cn.intellif.transaction.intelliftransaction.anotation.TxTransaction;
import cn.intellif.transaction.intelliftransaction.constant.Constant;
import cn.intellif.transaction.intelliftransaction.core.TransactionConnUtils;
import cn.intellif.transaction.intelliftransaction.core.netty.protocol.ProtocolUtils;
import cn.intellif.transaction.intelliftransaction.utils.ConnectionTimeOutUtils;
import cn.intellif.transaction.intelliftransaction.utils.SocketManager;
import cn.intellif.transaction.intelliftransaction.utils.WebUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TxTransactionAspect  implements Ordered {

    @Value("${intellif.transacton.timeout}")
    private Integer timeout;

    @Around("@annotation(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction)")
    public Object aroundWithTx(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    @Around("this(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction) && execution( * *(..))")
    public Object aroundWithTx2(ProceedingJoinPoint joinPoint) throws Throwable {
        return runTxTransaction(joinPoint);
    }

    private Object runTxTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        /**
         * 创建唯一标示
         */
        TransactionConnUtils.intKey();
        try{
            //将唯一表示告诉txmanger并开启超时机制
            SocketManager.getInstance().sendMsg(ProtocolUtils.ping());
            ConnectionTimeOutUtils.timeOut(TransactionConnUtils.getConnection(),timeout);
            Object result =   joinPoint.proceed();
           //发送成功信息告诉txmanager
            SocketManager.getInstance().sendMsg(ProtocolUtils.commit());
            return result;
        }catch (Exception e){
            //发送异常信息告诉txmanager
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            throw new RuntimeException(e);
        }finally {
            //发送关闭信息给txmanager
            SocketManager.getInstance().sendMsg(ProtocolUtils.clear());
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
        if(clazz.getAnnotation(TxTransaction.class)!=null){
            return joinPoint.proceed();
        }
        String token =   WebUtils.getRequest().getHeader(Constant.TRANSATION_TOKEN);
        if(token==null){
            return joinPoint.proceed();
        }
        TransactionConnUtils.initKey(token);
        try{
            //讲唯一表示告诉txManager
            SocketManager.getInstance().sendMsg(ProtocolUtils.ping());
            ConnectionTimeOutUtils.timeOut(TransactionConnUtils.getConnection(),timeout);
            return joinPoint.proceed();
        }catch (Exception e){
            //发送异常信息告诉txManager
            SocketManager.getInstance().sendMsg(ProtocolUtils.rollback());
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
