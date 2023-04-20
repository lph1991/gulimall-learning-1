package io.niceseason.gulimall.order.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gulimall.thread")
@Data
public class ThreadPoolConfigProperties {
//    配置线程池参数
    private int corePoolSize;
    private int maxPoolSize;
    private long keepAliveTime;

}
