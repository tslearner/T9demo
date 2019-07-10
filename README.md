### T9拨号盘搜索和排序算法

* * *
[TOC] 
#### 一. 背景
今日头条为何能突破bat的壁垒，很大程度在于它精确的推荐算法， 能够根据用户的喜爱推荐适合用户的资讯，不断根据用户的浏览记录构建用户的偏好生态圈，进而精准投放流量。

大家平常拨打电话应该都有用T9拨号盘吧，输入几个数字后一般会把当前通讯录匹配中的联系人显示出来并高亮显示命中的字符，根据输入的数字串显示哪些联系人数据以及这些联系人数据如果排序显得十分重要，站在用户角度，尽可能的少输入数字并能把用户想拨打的联系人优先显示到前列，这里面涉及的算法就尤为重要了，在此感谢产品mm对权重分配的打磨。

#### 二. T9匹配和排序思路

1. 加载本地通讯录，本地通讯录作为数据源；
2. 获取本地通话记录，通话记录作为其中一项影响因子；
3. 本地联系人的名字转化为T9键盘上对应的数字，根据输入的数字串进行匹配；
4. 根据首字母匹配、全拼匹配、号码匹配三种类型确认匹配的数据源；
5. 根据各项影响因子分别计算对应的权重，最终累加权重作为联系人排序的依据。

#### 三. 联系人实体类
拿名字曾轶可 拨号盘输入95举例，注意曾是多音字  
![T9拨号盘](https://github.com/tslearner/T9demo/blob/master/doc/t9.png)


**联系人SimpleContact**

| 字段 | 值 | 类型 | 描述 |
| --- | --- | --- |  --- |
| mName | 曾轶可 | String | 名字 |
| mNumber | 13912345678 | String | 号码 |
| isFirstMatch | true | boolean | 是否首字母匹配 |
| firstCharactorCounts |2  | int |  拼音匹配到多少汉字 |
| numberMatchId | -1 | int  | 号码匹配的下标 |
| pinyinMatchId | (10,13) | List<Integer> |  关键字在全拼中的下标 |
| startIndex  | 1 | int |keyword在contact首字母拼接字符串的起始位置 |
|searchType | 1 | int |搜索类型（ 拼音SEARCH_TYPE_PINYIN1，号码SEARCH_TYPE_NUMBER2） |
| PinYin |  | PinYin | 拼音类 |
| simplePinyinIndex | 0 | int | 记录用的是哪个首字母拼接字符串 |
| hightLighter | 12 | List<Integer> | 高亮的字符位置 |
| mSearchWeight | 0 | float | 权重 |
| spaceIndexList | null | List<Integer> | 名字中空格的下标 |



***

**拼音PinYin**

| 字段 | 值 | 类型 | 描述 |
| --- | --- | --- | --- |
| firstMatchId | 0 10 13 | List<Integer> | 首字母在全拼中的下标 |
| mNormalPinyin | zeng_ceng yi ke | String | words每个汉字的拼音拼接，并且每个汉字拼音之间用空格隔开 |
|mT9Pinyin  | 9364_2364 94 53 | String | 和normalPinyin对应，转化为T9上数字 |
| nameArray | zeng_ceng yi ke  | String[] |words中每个汉字的拼音  |
|normalPinyinFirstList  |zyk  cyk  | List<String> | 首字母 |
|simplePinyin  | zyk | String | words拼音首字母的拼接 |
| t9PinyinFirstList | 995 295  |List<String> |首字母对应的t9数字  |

#### 四. 匹配数据源逻辑
```java
/** 
* 根据关键字，返回搜索列表。 
* @param queryString  当前的搜索关键字 
* @param preInput    上一次的搜索关键字 
* @param searchCache  上一次的搜索结果列表 
* @param contactList   总搜索数据源列表
/
public static List<SimpleContact> searchT9StringInContacts(String queryString, String preInput, 
List<SimpleContact> searchCache,                                                           ArrayList<SimpleContact> contactList)
```
**以首字母-全拼-号码依次优先级匹配：**
1. 首字母
根据首字母在全拼中的下标firstMatchId字段匹配当前输入数字串，若前者包含后者，则首字母命中，获取首字母匹配个数和数字串在全拼中的下标，设为拼音类型。
2. 全拼
若1不符合，根据mT9Pinyin（全拼音转化为t9数字）匹配输入数字串，若前者包含后者，则全拼命中，获取数字串在全拼中的下标，设为拼音类型。
3. 号码
若1和2不符合，根据mNumber（号码）匹配输入数字串，若前者包含后者，则号码命中，获取数字串在号码中的起始匹配位置，设为号码类型。

**高亮规则**  
不管是首字母、全拼还是号码匹配，都必须是连续的，命中首字母则对应的汉字高亮，匹配到几个则几个高亮。

#### 五. 排序算法

*整体的排序优先级是根据各项影响因子来累积权值，根据最终权重大小排个高低。*

影响因子有：

* 联系人匹配类型（首字母匹配、全拼匹配、号码匹配）
* 完全匹配、非完全匹配
* 关键字的首字母在源数据的起始配置
* 名字长度
* 通话记录
* 名字高亮数量
* T9上的字母顺序

**根据各因子的权值、系数来控制算法的合理性**
```java
private static  int FirstMatch= 20;//首字母匹配初始值
private static  int AllFirstMatch=10000;//首字母全匹配
private static  int charMatch=15;//拼音匹配初始值
private static  int AllCharMatch=10000;//拼音完全匹配
private static  int numberMatch=10;//号码匹配初始值
private static  int AllNumberMatch=10000;//号码完全匹配
private static int NotAllCharMatchIni=600;//拼音字母非完全匹配初始值
private static int NotAllNumMatchIni=0;//号码非完全匹配初始值
private static  int Charorder=2;//字母顺序系数
private static  int StartIndexweight=1;//首字母位置系数
private static  float FirstCharactorCounts=0.5f;//首字母数量系数
private static  float NameCounts=0.05f;//名字数量系数
private static  int Fristchar=65;//第一位系数
private static  int Secondchar=15;//第二位系数
private static  int Thirdchar= 10;//第三位系数
private static  int CalllogNum=100;//通话纪录初始值
private static  int MaxCallLogNum=550;//通话记录最大数
private static int CalllogNumweight=1;//通话记录系数
private static int CharAsciiMax=127;//字母顺序最大值


```

**详细算法和各因子权值公式**  
![排序算法](https://github.com/tslearner/T9demo/blob/master/doc/T9排序算法.png)


#### 六. T9Demo工程
话不多说，直接上工程，包括T9搜索和排序相关的实体和操作类以及一个简陋的界面。  
![T9工程](https://github.com/tslearner/T9demo/blob/master/doc/t9project.png)


**工程链接:**
https://github.com/tslearner/T9demo

