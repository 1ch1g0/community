package com.ichigo.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component
public class SensitiveFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensitiveFilter.class);

    //敏感词替换符
    private static final String REPLACEMENT = "***";

    //前缀树根节点
    private TrieNode rootNode = new TrieNode();

    /**
     * @PostConstruct 表示在当前类实例化的时候自动调用
     */
    @PostConstruct
    public void init(){
        try (
                //使用类加载器获取敏感词文件的字节流
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                //将字节流转换为字符流再转换为缓冲流
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        ){
            //敏感词
            String keyword;
            //从流中读取敏感词
            while ((keyword = reader.readLine()) != null){
                //将敏感词添加到前缀树
                this.addKeyWord(keyword);
            }
        } catch (IOException e) {
            LOGGER.error("加载敏感词文件失败：" + e.getMessage());
        }
    }

    /**
     * 将一个敏感词添加到前缀树中
     * @param keyword
     */
    private void addKeyWord(String keyword){
        //创建节点
        TrieNode tempNode = rootNode;
        for(int i = 0; i < keyword.length(); i++){
            char c = keyword.charAt(i);
            //获取当前节点map中key为c的节点
            TrieNode subNode = tempNode.getSubNode(c);

            if(subNode == null){
                //说明下层节点中没有以c为key的节点
                //添加节点
                subNode = new TrieNode();
                tempNode.addSubNode(c, subNode);
            }

            //进入下一层（有这个节点，直接进入下一层继续判断。没这个节点，添加节点，再到下一层）
            tempNode = subNode;

            //到达敏感词尾部（叶子节点），添加结束标识
            if(i == keyword.length() - 1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    /**
     * 定义前缀树
     */
    private class TrieNode{
        //敏感词结束表示
        private boolean isKeywordEnd = false;

        //子节点，使用map实现，key为下级字符，value为下级节点
        private Map<Character, TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNode(Character c, TrieNode node){
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNode(Character c){
            return subNodes.get(c);
        }
    }


    /**
     * 过滤敏感词
     * @param text 待过滤的文本
     * @return  过滤后的文本
     */
    public String filter(String text){
        //判空
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int begin = 0;
        //指针3
        int position = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while(position < text.length()){
            char c = text.charAt(position);

            //跳过符号
            if(isSymbol(c)){
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if(tempNode == rootNode){
                    sb.append(c);
                    begin++;
                }
                //无论符号再开头或中间，指针3都向下走一步
                position++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNode(c);
            if(tempNode == null){
                //以begin开头的字符串不是敏感词
                sb.append(text.charAt(begin));
                //进入下一个位置（指针2向下走一步，指针3等于指针2）
                position = ++begin;
                //指针1重新指向根节点
                tempNode = rootNode;
            }else if(tempNode.isKeywordEnd()){
                //发现敏感词，将begin~position的字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置（指针3向下走一步，指针2等于指针3）
                begin = ++position;
                //指针1重新指向根节点
                tempNode = rootNode;
            }else{
                //检查下一个字符
                position++;
            }
        }

        //将最后一批字符计入结果
        sb.append(text.substring(begin));
        //返回过滤后的文本
        return sb.toString();
    }

    /**
     * 判断是否为符号
     * @param c
     * @return
     */
    public boolean isSymbol(Character c){
        //0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }
}
