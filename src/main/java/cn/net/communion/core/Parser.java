package cn.net.communion.core;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.net.communion.helper.FileHelper;
import cn.net.communion.helper.PropsReader;
import cn.net.communion.helper.RandomData;

public class Parser {
    private String value;
    private Map<String, String> map;
    private Pattern varPattern;
    private Pattern rulePattern;
    private Pattern poolPattern;
    private PropsReader props;
    private Map<String, String> poolFileMap = new HashMap<String, String>();
    static private Parser instance = null;

    private Parser() {
        varPattern = Pattern.compile("\\$var\\{(\\w+)\\}");
        rulePattern = Pattern.compile("\\$rule\\{([0-9a-zA-Z,]+)\\}");
        poolPattern = Pattern.compile("\\$pool\\{([0-9a-zA-Z.]+)\\}");
        props = PropsReader.getInstance();

    }

    static Parser getInstance(String value, Map<String, String> map) {
        if (instance == null) {
            instance = new Parser();
        }
        instance.setValue(value);
        instance.setMap(map);
        return instance;
    }

    static public boolean checkGrammar(String value) {
        return value.contains("$var") || value.contains("$rule") || value.contains("$pool");
    }

    public String execute() {
        parseVar().parseRule().parsePool();
        return value;
    }

    private Parser parseVar() {
        Matcher m = varPattern.matcher(value);
        while (m.find()) {
            String name = m.group(1);
            String propValue = props.getProperty("var." + name);
            value = value.replace(m.group(0), propValue != null ? propValue : this.map.get(name));
        }
        return this;
    }

    private Parser parseRule() {
        Matcher m = rulePattern.matcher(value);
        while (m.find()) {
            value = value.replace(m.group(0), getRuleData(m.group(1).split(",")));
        }
        return this;
    }

    private String getRuleData(String[] arr) {
        String content = props.getProperty("rule." + arr[0]);
        if (content != null) {
            return RandomData.getRuleData(content, arr.length < 2 ? 6 : Integer.parseInt(arr[1]));
        }
        return null;
    }

    private Parser parsePool() {
        Matcher m = poolPattern.matcher(value);
        while (m.find()) {
            value = value.replace(m.group(0), getPoolData(m.group(1)));
        }
        return this;
    }

    private String getPoolData(String name) {
        String content = props.getProperty("pool." + name);
        if (content != null) {
            return RandomData.getPoolData(content.split(","));
        } else {
            String poolContent = poolFileMap.get(name);
            if (poolContent == null) {
                poolContent = FileHelper.read("pool/" + name);
                if (poolContent == null) {
                    return null;
                }
                // System.out.println(poolContent);
                poolFileMap.put(name, poolContent);
            }
            return RandomData.getPoolData(poolContent.split(","));
        }
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Map<String, String> getMap() {
        return map;
    }

    public void setMap(Map<String, String> map) {
        this.map = map;
    }
}
