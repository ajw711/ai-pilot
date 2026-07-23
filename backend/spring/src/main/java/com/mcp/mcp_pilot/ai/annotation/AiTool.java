package com.mcp.mcp_pilot.ai.annotation;

import com.mcp.mcp_pilot.common.enums.ToolType;
import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Component
public @interface AiTool {
    ToolType value();
}
