package com.example.jpatest.config.P6spyLogFormatter;

import com.p6spy.engine.spy.P6SpyOptions;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * 김대호
 * P6spy SQL pretty
 * 추후 P6 사용이 성능을 저하시키는지 확인하여 저하된다 판단하면 삭제 (dependency 포함)
 */
@Configuration
public class P6spyLogMessageFormatConfiguration {

    @PostConstruct
    public void setLogMessageFormat(){
        P6SpyOptions.getActiveInstance().setLogMessageFormat(P6spySqlFormatConfiguration.class.getName());
    }
}
