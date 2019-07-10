package com.ts.t9demo.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/9 18:36
 * Description:联系人实体类
 */
public class SimpleContact {

    private String mName;//名字
    private String mNumber;//号码
    private boolean isFirstMatch;//是否首字母匹配
    private int firstCharactorCounts;//拼音匹配到多少汉字
    private int numberMatchId;//号码匹配的下标
    private List<Integer> pinyinMatchId;//关键字在全拼中的下标
    private int startIndex;//keyword在contact首字母拼接字符串的起始位置
    private int searchType;//搜索类型（ 拼音SEARCH_TYPE_PINYIN1，号码SEARCH_TYPE_NUMBER2）
    private int simplePinyinIndex;//记录用的是哪个首字母拼接字符串
    private List<Integer> hightLighter= new ArrayList<Integer>();//高亮的字符位置
    private PinYin pinyin;//拼音类
    private float mSearchWeight = 0;// 号码搜索时， 搜索的优先级。
    private List<Integer> spaceIndexList; // 名字中空格的下标

    public static final int SEARCH_TYPE_PINYIN = 2;
    public static final int SEARCH_TYPE_NUMBER = 3;

    public void setName(String name) {
        mName = name;
    }
    public String getName() {
        return mName;
    }

    public String getNumber() {
        return mNumber;
    }

    public void setNumber(String number) {
        mNumber = number;
    }

    public List<Integer> getPinyinMatchId() {
        return pinyinMatchId;
    }

    public void setPinyinMatchId(List<Integer> nameMatchId) {
        this.pinyinMatchId = nameMatchId;
    }

    public boolean isFirstMatch() {
        return isFirstMatch;
    }

    public void setFirstMatch(boolean isFirstMatch) {
        this.isFirstMatch = isFirstMatch;
    }

    public int getNumberMatchId() {
        return numberMatchId;
    }

    public void setNumberMatchId(int numberMatchId) {
        this.numberMatchId = numberMatchId;
    }

    public int getSimplePinyinMatchIndex() {
        return simplePinyinIndex;
    }

    public void setSimplePinyinMatchIndex(int value) {
        simplePinyinIndex = value;
    }

    public void setStartIndex(int index) {
        startIndex = index;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setFirstCharactorCounts(int firstCharactorCounts) {
        this.firstCharactorCounts = firstCharactorCounts;
    }

    public int getFirstCharactorCounts() {
        return firstCharactorCounts;
    }

    public void appendWeightLight(int id) {
        hightLighter.add(Integer.valueOf(id));
    }

    public void cleanWeightLight() {
        hightLighter.clear();
    }

    public List<Integer> getWeightLight() {
        return hightLighter;
    }

    public void setSearchType(int searchType) {
        this.searchType = searchType;
    }

    public int getSearchType() {
        return searchType;
    }
    public void setPinyin(PinYin pingyin) {
        this.pinyin = pingyin;
    }

    public PinYin getPinyin() {
        return pinyin;
    }

    public float getSearchWeight() {
        return this.mSearchWeight;
    }

    public void setSearchWeight(float weight) {
        this.mSearchWeight = weight;
    }
    public List<Integer> getSpaceIndexList() {
        return spaceIndexList;
    }

    public void setSpaceIndexList(List<Integer> list) {
        this.spaceIndexList = list;
    }
}
