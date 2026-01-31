package com.rhineai.framework

import cn.hutool.extra.spring.SpringUtil
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import

@ComponentScan(basePackages = ["com.rhine.framework.exception"])
//@MapperScan("com.rhine.framework.mapper")
@Import(SpringUtil::class)
@Configuration
class FrameworkConfig {
}