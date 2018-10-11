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
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TxTransactionAspect {

    @Value("intellif.transacton.timeout")
    private Integer timeout;

    @Around("@annotation(cn.intellif.transaction.intelliftransaction.anotation.TxTransaction)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {
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
    public Object aroud(ProceedingJoinPoint joinPoint) throws Throwable {
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
}
