package com.avira.commonlib.autoconfigure;

import com.avira.commonlib.web.CommonControllerAdvice;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(CommonControllerAdvice.class)
public class CommonExceptionHandlingAutoConfiguration {
}
