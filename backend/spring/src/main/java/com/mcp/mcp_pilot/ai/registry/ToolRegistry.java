package com.mcp.mcp_pilot.ai.registry;

import com.mcp.mcp_pilot.ai.annotation.AiTool;
import com.mcp.mcp_pilot.common.enums.ToolType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.support.AopUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class ToolRegistry {

    private final Map<ToolType, Object> registry = new EnumMap<>(ToolType.class);

    // ApplicationContext를 주입받아 특정 어노테이션이 붙은 빈만 타겟 조회
    public ToolRegistry(ApplicationContext context) {
        log.info("[ToolRegistry] @AiTool 마킹 빈 조회 및 캐싱");

        Map<String, Object> toolBeans = context.getBeansWithAnnotation(AiTool.class);

        for (Object bean : toolBeans.values()) {
            Class<?> targetClass = AopUtils.isAopProxy(bean) ? AopUtils.getTargetClass(bean) : bean.getClass();
            AiTool aiTool = targetClass.getAnnotation(AiTool.class);

            if (aiTool != null) {
                ToolType toolType = aiTool.value();
                registry.put(toolType, bean);
                log.info("[ToolRegistry] 캐싱: ToolType[{}] ➔ Bean[{}]", toolType.name(), targetClass.getSimpleName());
            }
        }
        log.info("[ToolRegistry] 캐싱 프로세스 완료. 등록된 총 도구 수: {}개", registry.size());
    }

    public Object[] resolve(List<ToolType> types) {
        if (types == null || types.isEmpty()) {
            return new Object[0];
        }

        return types.stream()
                .map( type -> {
                    Object tool = registry.get(type);
                    if (tool == null) {
                        throw new IllegalStateException(String.format(
                                "정의된 ToolType[%s]에 일치하는 @AiTool 빈이 레지스트리에 등록되지 않았습니다. " +
                                        "해당 도구 클래스 위에 @AiTool 어노테이션이 올바르게 붙어있는지 확인하세요.", type.name()));
                    }
                    return tool;
                })
                .toArray();
    }

}
