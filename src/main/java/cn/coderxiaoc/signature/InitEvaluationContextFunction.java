package cn.coderxiaoc.signature;

import org.springframework.expression.EvaluationContext;

@FunctionalInterface
public interface InitEvaluationContextFunction {
    void initEvaluationContext(EvaluationContext  context);
}
