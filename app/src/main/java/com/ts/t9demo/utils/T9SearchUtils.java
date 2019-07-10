package com.ts.t9demo.utils;

import android.text.TextUtils;
import android.util.Log;

import com.ts.t9demo.model.SimpleContact;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/9 19:55
 * Description:T9匹配和排序逻辑
 */
public class T9SearchUtils {
    public static final String TAG="T9SearchUtils";
    /**
     * 根据关键字，返回搜索列表。
     *
     * @param queryString  当前的搜索关键字
     * @param preInput    上一次的搜索关键字
     * @param searchCache  上一次的搜索结果列表
     * @param contactList   总搜索数据源列表
     * @return
     */
    public static List<SimpleContact> searchT9StringInContacts(String queryString, String preInput, List<SimpleContact> searchCache,
                                                               ArrayList<SimpleContact> contactList) {
        List<SimpleContact> resultDataList = new ArrayList<>();

        if (TextUtils.isEmpty(queryString)) {//关键字为空，返回。
            return resultDataList;
        }

        List<SimpleContact> sourceDataList;//数据源，
        if ((!TextUtils.isEmpty(preInput))
                && (queryString.length() > preInput.length())
                && (queryString.startsWith(preInput))
                && (searchCache != null)
                && (!searchCache.isEmpty())) {// 如果本次搜索结果为上次子集， 则 使用上次的搜索结果作为数据源。
            sourceDataList = new ArrayList<>(searchCache);
        } else {//
            sourceDataList = new ArrayList<>(contactList);
        }

        if (sourceDataList.size() <= 0) {//数据源为空， 返回
            return resultDataList;
        }

        String pattern = queryString;
        
        ArrayList<SimpleContact> mNameResults = new ArrayList<>();
        ArrayList<SimpleContact> mNumberResults = new ArrayList<>();
        for (int i = 0; i < sourceDataList.size(); i++) {
            SimpleContact value = sourceDataList.get(i);

            if (value == null )
                continue;
            value.setFirstMatch(false);
            value.setNumberMatchId(-1);
            value.setPinyinMatchId(null);
            value.setFirstCharactorCounts(0);
            value.setSearchType(-1);

            //首字母转化t9 数字组合 多音字处理
            List<String> simplePinyinList = value.getPinyin().getT9PinyinFirstList();
            
            //处理空格高亮问题
            List<Integer> spaceIndexList = getSpaceIndex(value);
            value.setSpaceIndexList(spaceIndexList);
            String simplePinyin = null;
            int indexSP;
            if ((simplePinyinList != null) && (simplePinyinList.size() > 0)) {
                indexSP = 0;
                //搜索关键字是否在t9 数字队列中
                for (String strTmp : simplePinyinList) {
                    if (strTmp.contains(pattern)) {
                        simplePinyin = strTmp;
                        value.setSimplePinyinMatchIndex(indexSP);
                        break;
                    }
                    indexSP += 1;
                }
            }
            //首字母匹配
            if (simplePinyin != null) {
                List<Integer> nameMatchId = new ArrayList<>();
                //首字母在全拼中的下标 注意字和字之间有空格
                List<Integer> first = value.getPinyin().getFirstMatchId();
              
                if (first != null) {
                    int vIndex = simplePinyin.indexOf(pattern);
                    value.setStartIndex(vIndex);
                    for (int j = 0; j < pattern.length(); j++) {
                        //关键字在全拼中的下标
                        nameMatchId.add(first.get(vIndex));
                        //首字母匹配个数
                        value.setFirstCharactorCounts(value.getFirstCharactorCounts()+1);
                        vIndex++;
                    }

                }

                value.setPinyinMatchId(nameMatchId);
                value.setFirstMatch(true);
                calWightLight(value);
                value.setSearchType(SimpleContact.SEARCH_TYPE_PINYIN);
                addMatchContact(mNameResults, value);
            } else {//全拼音t9搜索
                value.setStartIndex(0);
                value.setFirstMatch(false);
                boolean match = matchPinYinString(value, pattern, true);

                if (match) {
                    calWightLight(value);
                    value.setSearchType(SimpleContact.SEARCH_TYPE_PINYIN);
                    addMatchContact(mNameResults, value);
                }else {//号码匹配
                    String phone = value.getNumber();
                    int pos = phone.indexOf(pattern);
                    if (pos != -1) {
                        SimpleContact numberContact = value;
                        numberContact.setNumberMatchId(pos);
                        numberContact.setNumber(phone);
                        numberContact.setSearchType(SimpleContact.SEARCH_TYPE_NUMBER);
                        mNumberResults.add(numberContact);
                    }
                }
            }
        }
        
        resultDataList.addAll(mNameResults);//名称字段的搜索结果
        resultDataList.addAll(mNumberResults);//号码字段的搜索结果
        countSearchWeight(resultDataList, queryString);// 计算排序优先级。
        Collections.sort(resultDataList, new ContacInfoComparator());//排序
        return resultDataList;
    }

    public static class ContacInfoComparator implements Comparator<SimpleContact> {

        @Override
        public int compare(SimpleContact lhs, SimpleContact rhs) {

            int ret =  (rhs.getSearchWeight()-lhs.getSearchWeight())>0?1:(rhs.getSearchWeight()-lhs.getSearchWeight())==0?0:-1;

            return ret;
        }

    }

    /*
   获取名字中空格的下标， pinyin过滤掉空格了
    */
    private static List<Integer> getSpaceIndex(SimpleContact contact){
        List<Integer> list = new ArrayList<>();
        if(contact == null || TextUtils.isEmpty(contact.getName())){
            return list;
        }
        String name = contact.getName();
        int len = name.length();
        for(int i=0; i<len;i++){
            char ch = name.charAt(i);
            if(ch==' '){
                list.add(i);
            }
        }
        return list;

    }

    //处理空格的情况
    private static int getRealLoc(List<Integer> list,int loc){
        if(list==null || list.size()<=0){
            return loc;
        }
        int realLoc = loc;
        for(Integer integer:list){
            if(integer == null){
                integer = 0;
            }
            if(realLoc<integer){
                break;
            }else {
                realLoc++;
            }

        }
        return realLoc;
    }
    public static void addMatchContact(ArrayList<SimpleContact> mResults, SimpleContact value
                                       ) {
        mResults.add(value);
    }

    public static void calWightLight(SimpleContact item) {
        List nameMatchId = item.getPinyinMatchId();
        StringTokenizer reorder_str = new StringTokenizer(item.getPinyin().getNormalPinyin());

        int live_len = 0;
        int loc = 0;

        item.cleanWeightLight();
        while (reorder_str.hasMoreTokens()) {
            int str_len = reorder_str.nextToken().length();
            live_len += str_len;
            for (Iterator i$ = nameMatchId.iterator(); i$.hasNext(); ) {
                int matchid = ((Integer) i$.next()).intValue();
                if ((matchid >= live_len - str_len) && (matchid < live_len)) {
                    item.appendWeightLight(getRealLoc(item.getSpaceIndexList(),loc));
                    break;
                }
            }
            live_len++;
            loc++;
        }
    }

    public static boolean matchPinYinString(SimpleContact item, String pattern, boolean useT9) {
        StringTokenizer st = null;
        //t9全屏  多音字_分隔 啊我 为2_3 96
        if (useT9)
            st = new StringTokenizer(item.getPinyin().getT9Pinyin());
        else
            st = new StringTokenizer(item.getPinyin().getNormalPinyin());
        String strMatchTmp = pattern;
        boolean match = false;
        List nameMatchId = new ArrayList(); //下标匹配
        int countMatch = 0;
        int firstCountMatch = 0;//首字母匹配数
        while (st.hasMoreTokens()) {
            String next = st.nextToken();
            String[] aa = {next};
            if (next.contains("_")) {
                aa = next.split("_");
            }
            boolean bmatchTmp = false;
            for (int i = 0; i < aa.length; i++) {
                String strToken = aa[i];
                if (strToken.startsWith(strMatchTmp)) {
                    for (int j = 0; j < strMatchTmp.length(); j++)
                        nameMatchId.add(Integer.valueOf(j + countMatch));
                    countMatch += strMatchTmp.length();
                    match = true;
                    break;
                }
                

                if (strMatchTmp.startsWith(strToken)) {
                    strMatchTmp = strMatchTmp.substring(strToken.length());
                    for (int j = 0; j < strToken.length(); j++)
                        nameMatchId.add(Integer.valueOf(j + countMatch));
                    for (int m = i; m < aa.length; m++) {
                        countMatch += aa[m].length();
                        if (m < aa.length - 1) {
                            countMatch++;
                        }
                    }
                    bmatchTmp = true;
                    break;
                }
                countMatch += strToken.length();
                countMatch++;
            }
            firstCountMatch++;
            if (match)
                break;
            if (!bmatchTmp) {
                strMatchTmp = pattern;
                nameMatchId.clear();
            } else {
                countMatch++;
            }

        }
        if (match) {
            item.setPinyinMatchId(nameMatchId);
            //匹配的首字母数目
            item.setFirstCharactorCounts(firstCountMatch);
        }
        return match;
    }

    //拼音完全匹配
    public static boolean matchAllPinyin(SimpleContact item, String pattern, boolean useT9) {
        StringTokenizer st = null;
        //t9全屏  多音字_分隔 啊我 为2_3 96
        if (useT9)
            st = new StringTokenizer(item.getPinyin().getT9Pinyin());
        else
            st = new StringTokenizer(item.getPinyin().getNormalPinyin());
        String strMatchTmp = pattern;
        boolean flag = false;
        while (st.hasMoreTokens()) {
            flag = false;
            String next = st.nextToken();
            String[] aa = {next};
            if (next.contains("_")) {
                aa = next.split("_");
            }
            for (int i = 0; i < aa.length; i++) {
                String strToken = aa[i];
                if (strMatchTmp.startsWith(strToken)) {
                    strMatchTmp = strMatchTmp.substring(strToken.length());
                    flag = true;
                    break;
                }
            }
            if(!flag){//没匹配到
                return false;
            }

        }
        return true;

    }



    private static  int FirstMatch= 20;//首字母匹配初始值
    private static  int AllFirstMatch=10000;//首字母全匹配
    private static  int charMatch=15;//拼音匹配初始值
    private static  int AllCharMatch=10000;//拼音完全匹配
    private static  int numberMatch=10;//号码匹配初始值
    private static  int AllNumberMatch=10000;//号码完全匹配
    private static int NotAllCharMatchIni=600;//拼音字母非完全匹配初始值
    private static int NotAllNumMatchIni=0;//号码非完全匹配初始值
    private static  int Charorder=2;//字母顺序系数
    private static  int StartIndexweight=1;//首字母位置系数
    private static  float FirstCharactorCounts=0.5f;//首字母数量系数
    private static  float NameCounts=0.05f;//名字数量系数
    private static  int Fristchar=65;//第一位系数
    private static  int Secondchar=15;//第二位系数
    private static  int Thirdchar= 10;//第三位系数
    private static  int CalllogNum=100;//通话纪录初始值
    private static  int MaxCallLogNum=550;//通话记录最大数
    private static int CalllogNumweight=1;//通话记录系数
    private static int CharAsciiMax=127;//字母顺序最大值
    /**
     * 计算 搜索时的 联系人 权重。
     *
     * @param mAllResults
     * @param key
     */
    private static void countSearchWeight(List<SimpleContact> mAllResults, String key) {
        for (SimpleContact mResult:mAllResults) {
            float searchweight = 0;

            //首字母匹配
            if(mResult.isFirstMatch()){
                searchweight+=FirstMatch;
                Log.e(TAG,mResult.getName()+"+ 首字母匹配初始值"+FirstMatch);
                String simplePinYinLhs = null;
                List<String> simplePinYinList =mResult.getPinyin().getT9PinyinFirstList();
                int spIndex = mResult.getSimplePinyinMatchIndex();
                if ((simplePinYinList != null)
                        && (simplePinYinList.size() > spIndex))
                    simplePinYinLhs = simplePinYinList.get(spIndex);
                //首字母全部匹配中
                if(simplePinYinLhs!=null&&simplePinYinLhs.equals(key)){
                    searchweight+=AllFirstMatch;
                    Log.e(TAG,mResult.getName()+"+ 首字母全部匹配中"+AllFirstMatch);
                }else {
                    //首字母位置
                    searchweight+=NotAllCharMatchIni+(mResult.getStartIndex()==0?Fristchar:mResult.getStartIndex()==1?
                            Secondchar:mResult.getStartIndex()==2?Thirdchar:(10-mResult.getStartIndex())>0?(10-mResult.getStartIndex()):0)*StartIndexweight;
                    Log.e(TAG,mResult.getName()+"+ 首字母非完全匹配位置"+(NotAllCharMatchIni+(mResult.getStartIndex()==0?Fristchar:mResult.getStartIndex()==1?
                            Secondchar:mResult.getStartIndex()==2?Thirdchar:(10-mResult.getStartIndex())>0?(10-mResult.getStartIndex()):0)*StartIndexweight));
                }
            }else if(mResult.getNumberMatchId()==-1){
                //非首字母适配和号码适配时
                searchweight+=charMatch;
                Log.e(TAG,mResult.getName()+"+ 拼音匹配初始值"+charMatch);
                //拼音完全匹配
                if(matchAllPinyin(mResult,key,true)){
                    searchweight+=AllCharMatch;
                    Log.e(TAG,mResult.getName()+"+ 拼音完全匹配"+AllCharMatch);
                }else if(mResult.getPinyinMatchId()!=null&&mResult.getPinyinMatchId().size()>0){
                    //全拼音匹配位置 前三位分数多，4到10位仍加分 10位以后扣分
                    searchweight+=NotAllCharMatchIni+(mResult.getPinyinMatchId().get(0)==0?Fristchar:mResult.getPinyinMatchId().get(0)==1?
                            Secondchar:mResult.getPinyinMatchId().get(0)==2?Thirdchar:(10-mResult.getPinyinMatchId().get(0))>0?(10-mResult.getPinyinMatchId().get(0)):0)*StartIndexweight;
                    Log.e(TAG,mResult.getName()+"+ 拼音位置"+(NotAllCharMatchIni+(mResult.getPinyinMatchId().get(0)==0?Fristchar:mResult.getPinyinMatchId().get(0)==1?
                            Secondchar:mResult.getPinyinMatchId().get(0)==2?Thirdchar:(10-mResult.getPinyinMatchId().get(0))>0?(10-mResult.getPinyinMatchId().get(0)):0)*StartIndexweight));
                    //名字长度 最大值90
                    searchweight+=(10-mResult.getName().length())*NameCounts;
                    Log.e(TAG,mResult.getName()+"+ 名字长度"+(10-mResult.getName().length())*NameCounts);

                }

            }
            //通话纪录权重 主叫系数2 来电系数1 有通话纪录100起600止
            if(CallLogUtils.getWeightHashMap()!=null&&mResult.getNumber()!=null){
                Integer weightMap=CallLogUtils.getWeightHashMap().get(mResult.getNumber());
                int callWeight=0;
                if(weightMap==null){
                    weightMap=0;
                }

                if(weightMap>0){//有通话记录 初始值100  最大550
                    callWeight = CalllogNum+weightMap*CalllogNumweight;
                    searchweight+=callWeight>MaxCallLogNum?MaxCallLogNum:callWeight;
                    Log.e(TAG,mResult.getName()+"+ 通话纪录权重"+(callWeight>MaxCallLogNum?MaxCallLogNum:callWeight));
//
                }

            }
            //首字母匹配数量
            searchweight+=mResult.getFirstCharactorCounts()*FirstCharactorCounts;
            Log.e(TAG,mResult.getName()+"+ 首字母数量"+mResult.getFirstCharactorCounts()*FirstCharactorCounts);
            //字母顺序 最大值为60最小值为0
            if(mResult.getPinyin()!=null&&mResult.getPinyin().getNormalPinyin()!=null&&mResult.getPinyinMatchId()!=null&&mResult.getPinyinMatchId().size()>0){
                int charValue = getCharValue(mResult.getPinyin().getNormalPinyin().charAt(mResult.getPinyinMatchId().get(0)));
                searchweight+=(CharAsciiMax-charValue)*Charorder;

                Log.e(TAG,mResult.getName()+"+ 字母顺序"+mResult.getPinyin().getNormalPinyin().charAt(mResult.getPinyinMatchId().get(0)));
                Log.e(TAG,mResult.getName()+"+ 字母值"+charValue);
                Log.e(TAG,mResult.getName()+"+ 字母顺序"+(CharAsciiMax-charValue)*Charorder);
            }
            if(mResult.getNumberMatchId()!=-1){
                searchweight+=numberMatch;
                Log.e(TAG,mResult.getName()+"+ 号码匹配初始值"+numberMatch);
                //号码完全匹配
                if(key.equals(mResult.getNumber())){
                    searchweight+=AllNumberMatch;
                    Log.e(TAG,mResult.getName()+"+ 号码完全匹配"+AllNumberMatch);
                }else {
                    //号码位置 最大值为200
                    searchweight += NotAllNumMatchIni+(mResult.getNumberMatchId() == 0 ? Fristchar : mResult.getNumberMatchId() == 1 ?
                            Secondchar : Thirdchar);
                    Log.e(TAG, mResult.getName() + "+ 号码位置" + (NotAllNumMatchIni+(mResult.getNumberMatchId() == 0 ? Fristchar : mResult.getNumberMatchId() == 1 ?
                            Secondchar : Thirdchar)));
                }
            }

            Log.e(TAG,mResult.getName()+"+ 总数"+searchweight);
            mResult.setSearchWeight(searchweight);

        }
    }

    //按下4  有ghi4   4比i小
    private static int getCharValue(char number){
        //若是字母 返回ascii
        if(number-'a'>=0){
            return number;
        }
        int value;
        switch (number){
            case '2':
                value = 'c'+1;
                break;
            case '3':
                value = 'f'+1;
                break;
            case '4':
                value = 'i'+1;
                break;
            case '5':
                value = 'l'+1;
                break;
            case '6':
                value = 'o'+1;
                break;
            case '7':
                value = 's'+1;
                break;
            case '8':
                value = 'v'+1;
                break;
            case '9':
                value = 'z'+1;
                break;
            default:
                return CharAsciiMax;
        }
        return value;

    }
}
