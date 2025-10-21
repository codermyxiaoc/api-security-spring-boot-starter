package cn.coderxiaoc.signature;

import org.springframework.context.ApplicationContext;

public class DefaultParamsParse extends ParamsParseAbstract{
    private String delimiter = "|";
    private String splitter = "&";
    public DefaultParamsParse(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    public DefaultParamsParse(ApplicationContext applicationContext, String delimiter, String splitter) {
        super(applicationContext);
        this.delimiter = delimiter;
        this.splitter = splitter;
    }

    @Override
    protected void initEvaluationContext(InitEvaluationContextFunction initEvaluationContextFunction) {
        initEvaluationContextFunction.initEvaluationContext(this.context);
    }

    @Override
    public String splitter() {
        return splitter;
    }

    @Override
    public String delimiter() {
        return delimiter;
    }
}
