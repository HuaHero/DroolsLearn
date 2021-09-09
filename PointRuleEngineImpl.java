package com.huawei.hqh.service;

import com.huawei.hqh.entity.PointDomain;


import org.kie.api.KieServices;
import org.kie.api.event.rule.DefaultRuleRuntimeEventListener;
import org.kie.api.event.rule.ObjectInsertedEvent;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/*import org.springframework.context.ApplicationContext;*/

/**
 * 规则接口实现
 *
 * @author h00284904
 * @since 2021-08-31
 */
public class PointRuleEngineImpl {
    private static final PointRuleEngineImpl INSTANCE = new PointRuleEngineImpl();
    private KieServices kieServices;
    private KieContainer kieContainer;
    private static final Logger LOGGER = LoggerFactory.getLogger(PointRuleEngineImpl.class);
/*    private ApplicationContext applicationContext;*/

    private DefaultRuleRuntimeEventListener listener = new DefaultRuleRuntimeEventListener() {
        @Override
        public void objectInserted(ObjectInsertedEvent event) {
            PointDomain object = (PointDomain) event.getObject();
            String username = object.getUserName();
            Boolean isBirthday = object.isBirthDay();

           LOGGER.info("username:{}, isBirthDay:{}", username, isBirthday);
        }
    };

    public PointRuleEngineImpl() {
        initEngine();
    }

    public static  PointRuleEngineImpl getInstance() {
        return INSTANCE;
    }

    public void initEngine() {
        // 设置时间格式
        System.setProperty("drools.dateformat", "yyyy-MM-dd HH:mm:ss");
        kieServices = KieServices.Factory.get();
        kieContainer = kieServices.getKieClasspathContainer();
    }


    public void executeRuleEngine(String ruleBaseName) {
        KieSession kieSession = kieContainer.getKieBase(ruleBaseName).newKieSession();
        kieSession.addEventListener(listener);

        final PointDomain pointDomain = new PointDomain();
        pointDomain.setUserName("hellokity");
        pointDomain.setBackMondy(100d);
        pointDomain.setBuyMoney(500d);
        pointDomain.setBackNums(1);
        pointDomain.setBuyNums(5);
        pointDomain.setBillThisMonth(5);
        pointDomain.setBirthDay(true);
        pointDomain.setPoint(0L);
        kieSession.insert(pointDomain);
        int fireCnt = kieSession.fireAllRules();
        System.out.println("fireCnt:" + fireCnt);
        kieSession.dispose();

        System.out.println("执行完毕BillThisMonth："+pointDomain.getBillThisMonth());
        System.out.println("执行完毕BirthDay: " + pointDomain.isBirthDay());
        System.out.println("执行完毕BuyMoney："+pointDomain.getBuyMoney());
        System.out.println("执行完毕BuyNums："+pointDomain.getBuyNums());

        System.out.println("执行完毕规则引擎决定发送积分："+pointDomain.getPoint());
    }

}
