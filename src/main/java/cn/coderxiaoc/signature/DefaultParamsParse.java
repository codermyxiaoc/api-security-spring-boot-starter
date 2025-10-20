package cn.coderxiaoc.signature;

import org.springframework.context.ApplicationContext;

public class DefaultParamsParse extends ParamsParseAbstract{
    public DefaultParamsParse(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    protected void initEvaluationContext(InitEvaluationContextFunction initEvaluationContextFunction) {
        initEvaluationContextFunction.initEvaluationContext(this.context);
    }
}
