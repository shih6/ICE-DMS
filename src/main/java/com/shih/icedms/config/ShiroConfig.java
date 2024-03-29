package com.shih.icedms.config;

import com.shih.icedms.shiro.JWTFilter;
import com.shih.icedms.shiro.MyHashedCredentialsMatcher;
import com.shih.icedms.shiro.UserRealm;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {
    @Autowired
    MyHashedCredentialsMatcher myHashedCredentialsMatcher;
    @Bean("shiroFilterFactoryBean")
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();

        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JWTFilter());
        factoryBean.setFilters(filterMap);

        factoryBean.setSecurityManager(securityManager);
        /*
         * 自定义url规则
         * http://shiro.apache.org/web.html#urls-
         */
        Map<String, String> filterRuleMap = new LinkedHashMap<>();
        filterRuleMap.put("/setting/web", "anon");
        filterRuleMap.put("/user/login", "anon");
        filterRuleMap.put("/user/dingtalkAuth","anon");
        filterRuleMap.put("/user/login/**", "anon");
        filterRuleMap.put("/user/dingtalkAuth/**","anon");
        filterRuleMap.put("/matter/downloadByToken/**","anon");
        filterRuleMap.put("/onlyoffice/**","anon");
        filterRuleMap.put("/install/test","anon");

        filterRuleMap.put("/avatar/upload","jwt");
        filterRuleMap.put("/avatar/**","anon");
        filterRuleMap.put("/static/**","anon");

        //开放API文档接口
        filterRuleMap.put("/swagger-ui.html", "anon");
        filterRuleMap.put("/webjars/**","anon");
        filterRuleMap.put("/swagger-resources/**","anon");
        filterRuleMap.put("/v2/**","anon");
        //sql监控
        filterRuleMap.put("/druid/**","anon");
        // 所有请求通过JWT Filter
        filterRuleMap.put("/**", "jwt");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
    }

    @Bean
    public DefaultWebSecurityManager securityManager(UserRealm realm) {
        realm.setCredentialsMatcher(myHashedCredentialsMatcher);
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 使用自己的realm
        manager.setRealm(realm);
        /*
         * 关闭shiro自带的session，详情见文档
         * http://shiro.apache.org/session-management.html#SessionManagement-StatelessApplications%28Sessionless%29
         */
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);

        return manager;
    }
}
