package cn.coderxiaoc.signature;

import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class ParamsParseAbstract implements ParamsParse {
    private List<String> paramsSort;
    private Map<String, String> paramsMap;
    protected ApplicationContext applicationContext;
    private ExpressionParser parser;
    protected StandardEvaluationContext context;

    public ParamsParseAbstract(ApplicationContext applicationContext) {
        this.paramsSort = new ArrayList<>();
        this.paramsMap = new HashMap<>();
        this.applicationContext = applicationContext;
        this.parser = new SpelExpressionParser();
        this.context = new StandardEvaluationContext();
        context.setBeanResolver(new BeanFactoryResolver(applicationContext));
    }
    public EvaluationContext getContext() {
        return context;
    }
    protected abstract void initEvaluationContext(InitEvaluationContextFunction initEvaluationContextFunction);
    @Override
    public String parse(String params) {
        if (!StringUtils.hasText( params)) {
            return  "";
        }
        return doParse(params);
    }
    public String doParse(String params) {
        this.split(params);
        this.parseExpression();
        return this.assembleParams();
    }
    private void split(String params) {
        String[] split = params.split(splitter());
        for (String s : split) {
            this.paramsSort.add(s);
            this.paramsMap.put(s, "");
        }
    }

    public String splitter() {
        return "&";
    }
    public String delimiter() {
        return "|";
    }
    public void parseExpression() {
        for (String key : paramsMap.keySet()) {
            String value = parser.parseExpression(key).getValue(this.context, String.class);
            this.paramsMap.put(key, value);
        }
    }
    public String assembleParams() {
        ArrayList<String> result = new ArrayList<>();
        for (String params : this.paramsSort) {
            result.add(this.paramsMap.get(params));

        }
        return result.stream().collect(Collectors.joining(delimiter())).toString();
    }
}
