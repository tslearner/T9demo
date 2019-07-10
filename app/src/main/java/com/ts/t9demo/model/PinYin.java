package com.ts.t9demo.model;

import android.text.TextUtils;


import com.ts.t9demo.utils.PinyinUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Project: T9demo
 * Author: tianshuai
 * Date: 2019/7/10 8:57
 * Description:拼音实体类
 */
public class PinYin implements Serializable {

	private String simplePinyin;  //words拼音首字母的拼接
	private String indexKey;  //words中第一个字拼音的首字母
	private int firstWordType;
	private String[] nameArray;  //保存words中每个汉字的拼音
	private List<Integer> firstMatchId; //首字母在全拼中的下标
	private String mNormalPinyin;  //words每个汉字的拼音拼接，并且每个汉字拼音之间用空格隔开
	private String mT9Pinyin;  //和normalPinyin对应，通过ContactAccessor.nameToNumber来将每个字母转成9宫格输入法上的位置，用九宫格上的数字来对应。（eg：normalPinyin拼音为"he fei xin",转成mT9Pinyin则为"43 334 946"）
	private List<String> t9PinyinFirstList;//首字母对应的t9数字
	private List<String> normalPinyinFirstList;//首字母

	/**
	 * 创建拼音对象
	 * 
	 * @param words
	 *            转拼音的字符串
	 */
	public static PinYin buildPinYin(String words) {

		int index = 0;
		List<Integer> matchNameId = new ArrayList<Integer>();
		ArrayList<String> tokens = PinyinUtils.getPinyins(words);

		StringBuffer buffer = new StringBuffer();
		String[] nameArray = new String[tokens.size()];
		StringBuilder simplePinyinBuilder = new StringBuilder();
		ArrayList<String> strPinYinFirstArray = new ArrayList<String>();
		for (int i = 0, length = tokens.size(); i < length; i++) {
			String token = tokens.get(i);
//			Log.e("Pinyin","token:" + token);
			nameArray[i] = token;
			if (TextUtils.isEmpty(token)) {
				continue;
			}
			if (i > 0) {
				buffer.append(" ");
			}
			simplePinyinBuilder.append(token.charAt(0));
			matchNameId.add(Integer.valueOf(index));
			index += token.length() + 1;
			buffer.append(token);
			
			String[] aa = token.split("_");
			ArrayList<String> arrayAA = new ArrayList<String>();
            for(int p=0; p< aa.length; p++) {
            	String tmp = aa[p].substring(0, 1);
            	if(!arrayAA.contains(tmp)) {
            		arrayAA.add(tmp);
            		if(i>3) //前4个子计算多音，以后不计算多音，否则效率很低
            			break;
            	}
            }
			if (strPinYinFirstArray.size() <= 0) {
				for (int j = 0; j < arrayAA.size(); j++) {
					String str = arrayAA.get(j);
					if (!strPinYinFirstArray.contains(str))
						strPinYinFirstArray.add(str);
				}
			} else {
				if(i>3) {//不用分配了
					for (int m = 0; m < strPinYinFirstArray.size(); m++) {
						if(arrayAA.size()>0) {
							String str = strPinYinFirstArray.get(m) + arrayAA.get(0);
							strPinYinFirstArray.set(m, str);
						}
					}
				} else {
					ArrayList<String> strArrayTmp = new ArrayList<String>(strPinYinFirstArray.size()*aa.length);
					for (int m = 0; m < strPinYinFirstArray.size(); m++) {
						for (int j = 0; j < arrayAA.size(); j++) {
							String str = strPinYinFirstArray.get(m) + arrayAA.get(j);
							strArrayTmp.add(str);
						}
					}
					strPinYinFirstArray = strArrayTmp;
				}				
			}

		}
		String indexKey = "";
		if (!TextUtils.isEmpty(simplePinyinBuilder)) {
			indexKey = buffer.substring(0, 1);
		}
		PinYin py = new PinYin(words, nameArray, matchNameId, indexKey,
				simplePinyinBuilder.toString(),
				buffer.toString().toLowerCase(), PinyinUtils.nameToNumber(
						buffer.toString()),strPinYinFirstArray);
		return py;
	}

	private PinYin(String words, String[] nameArray, List<Integer> matchNameId,
                   String indexKey, String simplePinyin, String normalPinyin,
                   String mT9Pinyin, ArrayList<String> strPinYinFirstArray) {
		setNameArray(nameArray);
		setSimplePinyin(simplePinyin);
		setFirstMatchId(matchNameId);
		setIndexKey(indexKey);
		//
		if (!TextUtils.isEmpty(words)) {
			setFirstWordType(PinyinUtils.getFirstWordType(words.charAt(0)));
			if (getFirstWordType() == PinyinUtils.FIRST_WORD_IS_OTHER) {
				setIndexKey(PinyinUtils.INDEX_OTHER_KEY_WORD);
			}
		}
		//
		setT9Pinyin(mT9Pinyin);
		setNormalPinyin(normalPinyin);		
		normalPinyinFirstList = strPinYinFirstArray;
		t9PinyinFirstList = new ArrayList<String>();
//		Log.e("Pinyin", "strPinYinFirstArray:" + strPinYinFirstArray);
		for(String strPinyinFirst : normalPinyinFirstList) {
//			Log.e("Pinyin", "strPinyinFirst:" + strPinyinFirst);
//			Log.e("Pinyin", "nameTonumber:" + ContactAccessor.nameToNumber(strPinyinFirst,T9SearchUtils.getT9Map()));
			t9PinyinFirstList.add(PinyinUtils.nameToNumber(strPinyinFirst));
			
		}
	}

	private void setFirstMatchId(List<Integer> list) {
		firstMatchId = list;
	}

	public List<Integer> getFirstMatchId() {
		return firstMatchId;
	}

	public void setFirstWordType(int firstWordType) {
		this.firstWordType = firstWordType;
	}

	public int getFirstWordType() {
		return firstWordType;
	}

	public String getSimplePinyin() {
		return simplePinyin;
	}

	public void setSimplePinyin(String samplePingyin) {
		this.simplePinyin = samplePingyin;
	}

	public void setT9Pinyin(String mT9Pinyin) {
		this.mT9Pinyin = mT9Pinyin;
	}

	public List<String> getT9PinyinFirstList() {
		return t9PinyinFirstList;
	}

	public String getT9Pinyin() {
		return mT9Pinyin;
	}

	public String getNormalPinyin() {
		return mNormalPinyin;
	}

	public void setNormalPinyin(String mNormalPinYin) {
		this.mNormalPinyin = mNormalPinYin;		
	}

	public List<String> getNormalPinyinFirstList() {
		return normalPinyinFirstList;
	}

	public void setIndexKey(String sortKey) {
		this.indexKey = sortKey;
	}

	public String getIndexKey() {
		return indexKey;
	}

	public void setNameArray(String[] nameArray) {
		this.nameArray = nameArray;
	}

	public String[] getNameArray() {
		return nameArray;
	}

}
