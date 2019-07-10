package com.ts.t9demo.utils;

import android.content.Context;
import android.text.TextUtils;

import com.ts.t9demo.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;


public class PinyinUtils {
    private static String[] pinyin = null;
    private static byte[] mBuffer = null;

    private static char[][] sT9Map = (char[][]) null;

    /**
     * 首字母为英文
     */
    public static final int FIRST_WORD_IS_ENGLISH = 0;
    /**
     * 首字母为中文
     */
    public static final int FIRST_WORD_IS_CHINESE = 1;
    /**
     * 首字母为非英文和非中文
     */
    public static final int FIRST_WORD_IS_OTHER = 2;
    /**
     * 非英文和非中文的其他默认字母归类为"#"
     */
    public static final String INDEX_OTHER_KEY_WORD = "#";

    private static PinyinUtils instance;
    private static HashMap<String, String> mMultiHash = new HashMap<String, String>();


    public static void init(Context context) {
        pinyin = context.getResources().getStringArray(R.array.pinyin);
        try {
            initalize(context);
            initMultipleWords(context);
            getT9Map();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static char[][] initT9Map() {
        String[] t9Array = new String[]{"0", "1", "2abc", "3def", "4ghi", "5jkl", "6mno", "7pqrs", "8tuv", "9wxyz",
                "*", "#", "+", " ", "_"};
        char[][] sT9Map = new char[t9Array.length][];
        int rc = 0;
        for (String item : t9Array) {
            int cc = 0;
            sT9Map[rc] = new char[item.length()];
            for (char ch : item.toCharArray()) {
                sT9Map[rc][cc] = ch;
                cc++;
            }
            rc++;
        }
        return sT9Map;
    }

    public static char[][] getT9Map() {
        if (sT9Map == null)
            sT9Map = initT9Map();
        return sT9Map;
    }


    public static void initalize(Context context) throws IOException {
        InputStream mInputStream = null;
        mInputStream = context.getAssets().open("pinyin.dat");
        mBuffer = new byte[mInputStream.available()];
        mInputStream.read(mBuffer);
        mInputStream.close();
    }
    public static String nameToNumber(String name) {
        StringBuilder sb = new StringBuilder();
        int len = name.length();
        for (int i = 0; i < len; i++) {
            boolean matched = false;
            char ch = Character.toLowerCase(name.charAt(i));
            for (char[] row : sT9Map) {
                for (char a : row) {
                    if (ch == a) {
                        matched = true;
                        sb.append(row[0]);
                        break;
                    }
                }
                if (matched) {
                    break;
                }
            }
            if (!matched) {
                sb.append("-");// 找不到匹配时 修改-1为-
            }
        }
        return sb.toString();
    }
    /**
     * 获取多音字列表
     *
     * @param context
     * @throws IOException
     */
    public static void initMultipleWords(Context context) throws IOException {
        InputStream mInputStream = context.getAssets().open("duopinyin.txt");
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                mInputStream));
        String temp = null;

        while ((temp = reader.readLine()) != null) {

            int i = 0;
            String word = "";
            String pinYinBuf = "";
            StringTokenizer token = new StringTokenizer(temp, " ");
            while (token.hasMoreTokens()) {
                if (i == 0) {
                    word = token.nextElement().toString();
                } else {
                    pinYinBuf += token.nextElement().toString() + "_";
                    if (!token.hasMoreElements()) {
                        if (pinYinBuf.endsWith("_")) {
                            pinYinBuf = pinYinBuf.substring(0,
                                    pinYinBuf.length() - 1);
                        }
                        mMultiHash.put(word, pinYinBuf);
                    }
                }
                i++;
            }
            temp = null;
        }

        reader.close();
        reader = null;

        mInputStream.close();
        mInputStream = null;
    }

    /**
     *
     * @param words
     * @return
     */
    public static ArrayList<String> getPinyins(String words) {


        ArrayList<String> pinyin = new ArrayList<String>();
        if(TextUtils.isEmpty(words)){
            return pinyin;
        }
        char param[] = words.toCharArray();

        for (int i = 0; i < param.length; i++) {
            if (param[i] != ' ') {
                String temp = getPinyin(param[i]);
                pinyin.add(temp);
            }
        }

        return pinyin;

    }

    public static String getPinyin(char paramChar) {
        if(mBuffer == null){
            return null;
        }
        String str = null;
        if (paramChar == '_') {
            paramChar = 'ぁ';
        }
        str = mMultiHash.get(String.valueOf(paramChar));
        if (str != null)
            return str;
        if (paramChar < 0) {
            return String.valueOf(paramChar).toLowerCase();
        }
        int i = 19968;
        int j = 40869;
        // Object localObject = Integer.valueOf(paramChar);
        int i2 = paramChar;// ((Integer) localObject).intValue();
        if ((i2 >= i) && (i2 <= j)) {
            int i3 = (i2 - i) * 2;
            int i4 = mBuffer[i3] * 256;
            int i5 = i3 + 1;
            int l = ((mBuffer[i5] & 0x80) >> 7) * 128;
            int i6 = i4 + l;
            int i7 = i3 + 1;
            int i1 = mBuffer[i7] & 0x7F;
            int i8 = i6 + i1;
            str = pinyin[i8];
        } else {
            str = String.valueOf(paramChar);
        }
        return String.valueOf(str).toLowerCase();
    }

    /**
     *
     * @param words
     * @return
     */
    public static String getPinyin(String words) {
        ArrayList<String> tokens = getPinyins(words);
        StringBuilder pinyin = new StringBuilder();
        for (int i = 0; i < tokens.size(); i++) {
            pinyin.append(tokens.get(i));
        }
        return pinyin.toString();
    }

    /**
     * 将汉字转换成全拼音
     *
     * @param words
     * @return
     */
    public static String getPinyinsString(String words) {

        if (TextUtils.isEmpty(words)||words.trim().length()==0) {
            return "";
            //throw new NullPointerException("words is null!");
        }

        StringBuffer stringBuff = new StringBuffer();
        char param[] = words.toCharArray();

        for (int i = 0; i < param.length; i++) {
            if (param[i] != ' ') {
                String temp = getPinyinVoice(param[i]);
                stringBuff.append(temp + ",");
            }
        }
        return stringBuff.substring(0, stringBuff.length() - 1).toString();

    }

    public static String getPinyinVoice(char paramChar) {
        String str = null;
        if (paramChar < 0) {
            return String.valueOf(paramChar).toLowerCase();
        }
        int i = 19968;
        int j = 40869;
        // Object localObject = Integer.valueOf(paramChar);
        int i2 = paramChar;// ((Integer) localObject).intValue();
        if ((i2 >= i) && (i2 <= j)) {
            int i3 = (i2 - i) * 2;
            int i4 = mBuffer[i3] * 256;
            int i5 = i3 + 1;
            int l = ((mBuffer[i5] & 0x80) >> 7) * 128;
            int i6 = i4 + l;
            int i7 = i3 + 1;
            int i1 = mBuffer[i7] & 0x7F;
            int i8 = i6 + i1;
            str = pinyin[i8];
        } else {
            str = String.valueOf(paramChar);
        }
        return String.valueOf(str).toLowerCase();
    }

    /**
     * 判断是否中文字符
     *
     * @param c 待判断的字符
     * @return true 表示是中文字符，否则false
     */
    public static boolean isChinese(char c) {

        Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
        if (ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || ub == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A) {
            return true;
        }
        return false;
    }
    /**
     * 判断是否英文字符
     *
     * @param c 待判断的字符
     * @return true 表示是英文字符，否则false
     */
    public static boolean isEnglish(char c) {
        if ((c >= 65 && c <= 90) || (c >= 97 && c <= 122)) {
            return true;
        }
        return false;
    }

    /**
     * 获取首字母类型
     *
     * @param word 判断的字符
     * @return FIRST_WORD_IS_ENGLISH=0,FIRST_WORD_IS_CHINESE=1,FIRST_WORD_IS_OTHER
     * =2
     */
    public static int getFirstWordType(char word) {
        int typeCode;
        if (isChinese(word)) {
            typeCode = FIRST_WORD_IS_CHINESE;
        } else if (isEnglish(word)) {
            typeCode = FIRST_WORD_IS_ENGLISH;
        } else {
            typeCode = FIRST_WORD_IS_OTHER;
        }

        return typeCode;
    }
}
