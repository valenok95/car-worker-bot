package ru.wallentos.carworker.configuration;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import ru.wallentos.carworker.model.Province;

@Data
@Configuration
public class ConfigDataPool {
    @Value("${ru.wallentos.carworker.client-request-spreedsheet-id:test}")
    public String clientRequestSpreedSheetId;
    @Value("${ru.wallentos.carworker.admin-list}")
    public List<String> adminList;
    @Value("${ru.wallentos.carworker.client-request-group-id}")
    public long clientRequestGroupId;
    @Value("${ru.wallentos.carworker.enable-client-request:false}")
    public boolean enableClientRequest;
    @Value("${ru.wallentos.carworker.korex-province-matrix:false}")
    public boolean isKorexProvinceMatrix;
    @Value("${ru.wallentos.carworker.manager-bot:false}")
    public boolean isManagerBot;
    @Value(value = "${ru.wallentos.carworker.currencies}")
    private List<String> currencies;
    @Value(value = "${ru.wallentos.carworker.white-manager-list:}")
    private List<String> whiteManagerList;
    @Value(value = "${ru.wallentos.carworker.parent-link:link}")
    private String parentLink;
    public static final String EUR = "EUR";
    public static final String RUB = "RUB";
    public static final String USD = "USD";
    public static final String KRW = "KRW";
    public static final String CNY = "CNY";
    public static final String CONFIRM_MAILING_BUTTON = "Подтвердить";
    public static final String CANCEL_MAILING_BUTTON = "Отменить";
    public static final String LINK_BUTTON = "Расчёт по ссылке";
    public static final String AUCTION_BUTTON = "Расчёт ставки на аукционе";
    public static final String MANUAL_BUTTON = "Расчёт вручную";
    @Value("${ru.wallentos.carworker.exchange-coefficient}")
    public double coefficient;
    @Value("${ru.wallentos.carworker.auction-coefficient:0}")
    public double auctionCoefficient;
    @Value("${ru.wallentos.carworker.extra-pay-china.cny}")
    public int EXTRA_PAY_AMOUNT_CHINA_CNY;
    @Value("${ru.wallentos.carworker.extra-pay-china.rub}")
    public int EXTRA_PAY_AMOUNT_CHINA_RUB;
    @Value("${ru.wallentos.carworker.enable-krw-link-mode:false}")
    public boolean enableKrwLinkMode;
    @Value("${ru.wallentos.carworker.enable-cny-link-mode:false}")
    public boolean enableCnyLinkMode;
    @Value("${ru.wallentos.carworker.enable-krw-auction-mode:false}")
    public boolean enableKrwAuctionMode;
    @Value("${ru.wallentos.carworker.disable-double-convertation:false}")
    public boolean disableDoubleConvertation;
    @Value("${ru.wallentos.carworker.extra-pay-corea.krw}")
    public int EXTRA_PAY_AMOUNT_KOREA_KRW;
    @Value("${ru.wallentos.carworker.extra-pay-corea.rub}")
    public int EXTRA_PAY_AMOUNT_KOREA_RUB;
    public static Map<String, Double> manualConversionRatesMapInRubles = new HashMap<>();
    public static final int NEW_MID_CAR_RECYCLING_FEE = 970_000;
    public static final int NEW_BIG_CAR_RECYCLING_FEE = 1_235_200;
    public static final int NORMAL_MID_CAR_RECYCLING_FEE = 1_485_000;
    public static final int NORMAL_BIG_CAR_RECYCLING_FEE = 1_623_800;
    public static final int NEW_CAR_RECYCLING_FEE = 3400;
    public static final int OLD_CAR_RECYCLING_FEE = 5200;
    public static final int CUSTOMS_VALUE_1 = 200_000;
    public static final int FEE_RATE_1 = 775;
    public static final int CUSTOMS_VALUE_2 = 450_000;
    public static final int FEE_RATE_2 = 1550;
    public static final int CUSTOMS_VALUE_3 = 1_200_000;
    public static final int FEE_RATE_3 = 3100;
    public static final int CUSTOMS_VALUE_4 = 2_700_000;
    public static final int FEE_RATE_4 = 8530;
    public static final int CUSTOMS_VALUE_5 = 4_200_000;
    public static final int FEE_RATE_5 = 12000;
    public static final int CUSTOMS_VALUE_6 = 5_500_000;
    public static final int FEE_RATE_6 = 15500;
    public static final int CUSTOMS_VALUE_7 = 7_000_000;
    public static final int FEE_RATE_7 = 20000;
    public static final int CUSTOMS_VALUE_8 = 8_000_000;
    public static final int FEE_RATE_8 = 23000;
    public static final int CUSTOMS_VALUE_9 = 9_000_000;
    public static final int FEE_RATE_9 = 25000;
    public static final int CUSTOMS_VALUE_10 = 10_000_000;
    public static final int FEE_RATE_10 = 27000;
    public static final int LAST_FEE_RATE = 30000;
    public static final int NEW_CAR_PRICE_DUTY_1 = 8500;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_1 = new AbstractMap.SimpleEntry<>(0.54, 2.5);
    public static final int NEW_CAR_PRICE_DUTY_2 = 16700;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_2 = new AbstractMap.SimpleEntry<>(0.48, 3.5);
    public static final int NEW_CAR_PRICE_DUTY_3 = 42300;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_3 = new AbstractMap.SimpleEntry<>(0.48, 5.5);
    public static final int NEW_CAR_PRICE_DUTY_4 = 84500;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_4 = new AbstractMap.SimpleEntry<>(0.48, 7.5);
    public static final int NEW_CAR_PRICE_DUTY_5 = 169000;
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_FLAT_RATE_5 = new AbstractMap.SimpleEntry<>(0.48, 15d);
    public static final Map.Entry<Double, Double> NEW_CAR_PRICE_MAX_FLAT_RATE = new AbstractMap.SimpleEntry<>(0.48, 20d);
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_1 = 1.5;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_2 = 1.7;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_3 = 2.5;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_4 = 2.7;
    public static final int NORMAL_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_5 = 3;
    public static final double NORMAL_CAR_PRICE_FLAT_RATE_MAX = 3.6;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_1 = 1000;
    public static final double OLD_CAR_PRICE_FLAT_RATE_1 = 3;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_2 = 1500;
    public static final double OLD_CAR_PRICE_FLAT_RATE_2 = 3.2;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_3 = 1800;
    public static final double OLD_CAR_PRICE_FLAT_RATE_3 = 3.5;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_4 = 2300;
    public static final double OLD_CAR_PRICE_FLAT_RATE_4 = 4.8;
    public static final int OLD_CAR_ENGINE_VOLUME_DUTY_5 = 3000;
    public static final double OLD_CAR_PRICE_FLAT_RATE_5 = 5;
    public static final double OLD_CAR_PRICE_FLAT_RATE_MAX = 5.7;

    public static final String NEW_CAR = "До 3 лет";
    public static final String NORMAL_CAR = "От 3 до 5 лет";
    public static final String OLD_CAR = "От 5 лет";
    public static final String CANCEL_BUTTON = "Отмена";
    public static final String RESET_MESSAGE = "Рассчитать стоимость автомобиля";
    public static final String TO_SET_CURRENCY_MENU = "Меню установки валюты";
    public static final String MANAGER_MESSAGE = "Связаться с менеджером";
    public static final String CLIENT_REQUEST_BUTTON = "Оставить заявку";

    /**
     * Карта рассчёта таможенной стоимости.
     */
    public static final Map<Integer, Integer> feeRateMap = new LinkedHashMap<>() {
        {
            put(CUSTOMS_VALUE_1, FEE_RATE_1);
            put(CUSTOMS_VALUE_2, FEE_RATE_2);
            put(CUSTOMS_VALUE_3, FEE_RATE_3);
            put(CUSTOMS_VALUE_4, FEE_RATE_4);
            put(CUSTOMS_VALUE_5, FEE_RATE_5);
            put(CUSTOMS_VALUE_6, FEE_RATE_6);
            put(CUSTOMS_VALUE_7, FEE_RATE_7);
            put(CUSTOMS_VALUE_8, FEE_RATE_8);
            put(CUSTOMS_VALUE_9, FEE_RATE_9);
            put(CUSTOMS_VALUE_10, FEE_RATE_10);
        }
    };

    /**
     * Карта рассчёта размера пошлины для нового автомобиля.
     */
    public static final Map<Integer, Map.Entry<Double, Double>> newCarCustomsMap = new LinkedHashMap<>() {
        {
            put(NEW_CAR_PRICE_DUTY_1, NEW_CAR_PRICE_FLAT_RATE_1);
            put(NEW_CAR_PRICE_DUTY_2, NEW_CAR_PRICE_FLAT_RATE_2);
            put(NEW_CAR_PRICE_DUTY_3, NEW_CAR_PRICE_FLAT_RATE_3);
            put(NEW_CAR_PRICE_DUTY_4, NEW_CAR_PRICE_FLAT_RATE_4);
            put(NEW_CAR_PRICE_DUTY_5, NEW_CAR_PRICE_FLAT_RATE_5);
        }
    };
    /**
     * Карта рассчёта размера пошлины для автомобиля от 3 до 5 лет.
     */
    public static final Map<Integer, Double> normalCarCustomsMap = new LinkedHashMap<>() {
        {
            put(NORMAL_CAR_ENGINE_VOLUME_DUTY_1, NORMAL_CAR_PRICE_FLAT_RATE_1);
            put(NORMAL_CAR_ENGINE_VOLUME_DUTY_2, NORMAL_CAR_PRICE_FLAT_RATE_2);
            put(NORMAL_CAR_ENGINE_VOLUME_DUTY_3, NORMAL_CAR_PRICE_FLAT_RATE_3);
            put(NORMAL_CAR_ENGINE_VOLUME_DUTY_4, NORMAL_CAR_PRICE_FLAT_RATE_4);
            put(NORMAL_CAR_ENGINE_VOLUME_DUTY_5, NORMAL_CAR_PRICE_FLAT_RATE_5);
        }
    };
    /**
     * Карта рассчёта размера пошлины для автомобиля от 5 лет.
     */
    public static final Map<Integer, Double> oldCarCustomsMap = new LinkedHashMap<>() {
        {
            put(OLD_CAR_ENGINE_VOLUME_DUTY_1, OLD_CAR_PRICE_FLAT_RATE_1);
            put(OLD_CAR_ENGINE_VOLUME_DUTY_2, OLD_CAR_PRICE_FLAT_RATE_2);
            put(OLD_CAR_ENGINE_VOLUME_DUTY_3, OLD_CAR_PRICE_FLAT_RATE_3);
            put(OLD_CAR_ENGINE_VOLUME_DUTY_4, OLD_CAR_PRICE_FLAT_RATE_4);
            put(OLD_CAR_ENGINE_VOLUME_DUTY_5, OLD_CAR_PRICE_FLAT_RATE_5);
        }
    };

    /**
     * Карта китайских провинций и соответствующих им ценников, для расчёта по ссылке.
     * Имя провинции - наценка в ЮАНях.
     */
    public static final Map<String, Province> provincePriceMap = new LinkedHashMap<>() {
        {
            put("安徽", new Province("Anhui 安徽", 7449));
            put("甘肃", new Province("Gansu 甘肃", 9096));
            put("吉林", new Province("Jilin 吉林", 1803));
            put("广东", new Province("Guangdong 广东", 10998));
            put("贵州", new Province("Guizhou 贵州", 10857));
            put("辽宁", new Province("Liaoning 辽宁", 2673));
            put("四川", new Province("Sichuan 四川", 10215));
            put("福建", new Province("Fujian 福建", 9927));
            put("海南", new Province("Hainan 海南", 12492));
            put("湖北", new Province("Hubei 湖北", 8085));
            put("湖南", new Province("Hunan 湖南", 9066));
            put("河北", new Province("Hebei 河北", 5523));
            put("河南", new Province("Henan 河南", 6669));
            put("江西", new Province("Jiangxi 江西", 8643));
            put("江苏", new Province("Jiangsu 江苏", 7296));
            put("青海", new Province("Qinghai 青海", 9657));
            put("山东", new Province("Shandong 山东", 5598));
            put("山西", new Province("Shanxi 山西", 6096));
            put("陕西", new Province("Shaanxi 陕西", 8088));
            put("云南", new Province("Yunnan 云南", 12369));
            put("北京", new Province("Beijing 北京", 4740));
            put("上海市", new Province("Shanghai 上海市", 7875));
            put("天津", new Province("Tianjin 天津", 9010));
            put("大庆", new Province("Daqing 大庆", 1935));
            put("哈尔滨", new Province("Harbin 哈尔滨", 1425));
            put("牡丹江", new Province("Mudanjiang 牡丹江", 465));
            put("常熟", new Province("Changshu 常熟", 8346));
            put("常州", new Province("Changzhou 常州", 7479));
            put("昆山", new Province("Kunshan 昆山", 7818));
            put("溧阳", new Province("Liyang 溧阳", 7536));
            put("南京", new Province("Nanjing 南京", 5771));
            put("南通", new Province("Nantong 南通", 6991));
            put("苏州", new Province("Suzhou 苏州", 7215));
            put("台州", new Province("Taizhou 台州", 8005));
            put("无锡", new Province("Wuxi 无锡", 7692));
            put("大连", new Province("Dalian 大连", 3825));
            put("徐州", new Province("Xuzhou 徐州", 8205));
            put("盐城", new Province("Yancheng 盐城", 7367));
            put("扬州市", new Province("Yangzhou 扬州市", 7221));
            put("浙江", new Province("Zhejiang 浙江", 8070));
            put("德清", new Province("Deqing 德清", 7968));
            put("阜阳", new Province("Fuyang 阜阳", 7035));
            put("海宁", new Province("Haining 海宁", 7998));
            put("杭州", new Province("Hangzhou 杭州", 8067));
            put("湖州", new Province("Huzhou 湖州", 7851));
            put("嘉兴", new Province("Jiaxing 嘉兴", 7908));
            put("凌海", new Province("Linhai 凌海", 5935));
            put("宁波", new Province("Ningbo 宁波", 9814));
            put("温州", new Province("Wenzhou 温州", 9562));
            put("乌镇", new Province("Wuzhen 乌镇", 7920));
            put("义乌", new Province("Yiwu 义乌", 8457));
            put("舟山", new Province("Zhoushan 舟山", 8526));
            put("长春", new Province("Changchun 长春", 5139));
            put("鞍山", new Province("Anshan 鞍山", 3000));
            put("丹东", new Province("Dandong 丹东", 2769));
            put("沈阳", new Province("Shenyang 沈阳", 2679));
            put("西宁", new Province("Xining 西宁", 9648));
            put("敦煌", new Province("Dunhuang 敦煌", 11382));
            put("兰州", new Province("Lanzhou 兰州", 9060));
            put("西安", new Province("Xian 西安", 6600));
            put("长治", new Province("Changzhi 长治", 6456));
            put("大同", new Province("Datong 大同", 5742));
            put("太原", new Province("Taiyuan 太原", 6132));
            put("沧州", new Province("Cangzhou 沧州", 4986));
            put("廊坊", new Province("Langfang 廊坊", 4746));
            put("秦皇岛", new Province("Qinhuangdao 秦皇岛", 3897));
            put("石家庄", new Province("Shijiazhuang 石家庄", 5535));
            put("唐山", new Province("Tangshan 唐山", 6600));
            put("成都", new Province("Chengdu 成都", 10068));
            put("都江堰", new Province("Dujianguan 都江堰", 10110));
            put("峨眉山", new Province("Emeishan 峨眉山", 10578));
            put("九寨沟", new Province("Jiuzhaigou 九寨沟", 9942));
            put("乐山", new Province("Leshan 乐山", 10485));
            put("绵阳", new Province("Mianyang 绵阳", 9693));
            put("西昌", new Province("Xichang 西昌", 11340));
            put("保定", new Province("Baoding 保定", 5139));
            put("武汉", new Province("Wuhan 武汉", 8094));
            put("宜昌", new Province("Yichang 宜昌", 8529));
            put("登封", new Province("Dengfeng 登封", 6969));
            put("开封", new Province("Kaifeng 开封", 6591));
            put("洛阳", new Province("Luoyang 洛阳", 7056));
            put("南阳", new Province("Nanyang 南阳", 7458));
            put("新乡", new Province("Xinxiang 新乡", 6501));
            put("郑州", new Province("Zhengzhou 郑州", 6738));
            put("东营", new Province("Dongying 东营", 5454));
            put("济南", new Province("Jinan 济南", 5640));
            put("临沂", new Province("Linyi 临沂", 6147));
            put("青岛", new Province("Qingdao 青岛", 6210));
            put("日照", new Province("Rizhao 日照", 6258));
            put("泰安", new Province("Taian 泰安", 5784));
            put("潍坊", new Province("Weifang 潍坊", 5796));
            put("威海", new Province("Weihai 威海", 4380));
            put("烟台", new Province("Yantai 烟台", 4365));
            put("淄博", new Province("Zibo 淄博", 5502));
            put("蚌埠", new Province("Bengbu 蚌埠", 7047));
            put("巢湖", new Province("Chaohu 巢湖", 7545));
            put("合肥", new Province("Hefei 合肥", 7440));
            put("黄山", new Province("Huangshan 黄山", 8313));
            put("芜湖", new Province("Wuhu 芜湖", 7614));
            put("昆明", new Province("Kunming 昆明", 12405));
            put("丽江", new Province("Lijiang 丽江", 12381));
            put("香格里拉", new Province("Shangri-la 香格里拉", 14283));
            put("贵阳", new Province("Guiyang 贵阳", 10857));
            put("长沙", new Province("Changsha 长沙", 9069));
            put("韶山", new Province("Shaoshan 韶山", 9192));
            put("岳阳", new Province("Yueyang 岳阳", 8649));
            put("张家界", new Province("Zhangjiajie 张家界", 9300));
            put("赣州", new Province("Ganzhou 赣州", 9867));
            put("吉安", new Province("Ji\"an 吉安", 9285));
            put("景德镇", new Province("Jingdezhen 景德镇", 8436));
            put("九江", new Province("Juijiang 九江", 8292));
            put("南昌", new Province("Nangchang 南昌", 8646));
            put("东莞", new Province("Dongguan 东莞", 11112));
            put("佛山", new Province("Foshan 佛山", 11196));
            put("广州", new Province("Guanzhou 广州", 10968));
            put("惠州", new Province("Huizhoy 惠州", 10836));
            put("江门", new Province("Jiangmen 江门", 11319));
            put("清远", new Province("Qinguan 清远", 10884));
            put("汕头", new Province("Shantou 汕头", 10794));
            put("韶关", new Province("Shaoguan 韶关", 10371));
            put("深圳", new Province("Shenzhen 深圳", 11160));
            put("湛江", new Province("Zhanjiang 湛江", 12030));
            put("中山", new Province("Zhongshan 中山", 7564));
            put("珠海", new Province("Zhuhai 珠海", 11400));
            put("福州", new Province("Fuzhou 福州", 9966));
            put("靖江", new Province("Jingjiang 靖江", 10210));
            put("龙阳君", new Province("Longyang 龙阳君", 10191));
            put("宁德", new Province("Ningde 宁德", 9666));
            put("泉州", new Province("Quanzhou 泉州", 10437));
            put("石狮", new Province("Shishi 石狮", 10500));
            put("厦门市", new Province("Xiamen 厦门市", 9138));
            put("哈密地", new Province("Hami 哈密地", 11310));
            put("赤峰", new Province("Chifeng 赤峰", 3675));
            put("滿洲", new Province("Manchuria 滿洲", 4344));
            put("乌海", new Province("Wuhai 乌海", 7842));
            put("北海", new Province("Beihai 北海", 12012));
            put("桂林", new Province("Guilin 桂林", 10488));
            put("贺州", new Province("Hezhou 贺州", 10124));
            put("柳州", new Province("Luizhou 柳州", 10938));
            put("南宁", new Province("Nanning 南宁", 15767));
            put("阳朔县", new Province("Yangshuo 阳朔县", 10710));
            put("银川", new Province("Yinchuan 银川", 8301));
            put("重庆", new Province("Chongqing 重庆", 7713));
            put("儋州", new Province("Danzhou 儋州", 9735));
            put("海口", new Province("Haikou 海口", 8937));
            put("三亚", new Province("Sanya 三亚", 13347));
            put("万宁", new Province("Wanning 万宁", 12993));
            put("五指山", new Province("Wuzhishan 五指山", 13128));
        }
    };


    /**
     * Карта китайских провинций и соответствующих им ценников, для расчёта по ссылке.
     * Имя провинции - наценка в ЮАНях.
     */
    public static final Map<String, Province> provincePriceMapForKorex = new LinkedHashMap<>() {
        {
            put("鞍山", new Province("Anshan 鞍山", 3006));
            put("保定", new Province("Baoding 保定", 5121));
            put("北海", new Province("Beihai 北海", 12066));
            put("北京", new Province("Beijing 北京", 4725));
            put("蚌埠", new Province("Bengbu 蚌埠", 7032));
            put("沧州", new Province("Cangzhou 沧州", 4965));
            put("长春", new Province("Changchun 长春", 1836));
            put("长沙", new Province("Changsha 长沙", 9048));
            put("常熟", new Province("Changshu 常熟", 7650));
            put("长治", new Province("Changzhi 长治", 6420));
            put("常州", new Province("Changzhou 常州", 7383));
            put("巢湖", new Province("Chaohu 巢湖", 7509));
            put("成都", new Province("Chengdu 成都", 10053));
            put("赤峰", new Province("Chifeng 赤峰", 3687));
            put("重庆", new Province("Chongqing 重庆", 9924));
            put("大连", new Province("Dalian 大连", 3612));
            put("丹东", new Province("Dandong 丹东", 2775));
            put("儋州", new Province("Danzhou 儋州", 12819));
            put("大庆", new Province("Daqing 大庆", 1941));
            put("大同", new Province("Datong 大同", 5616));
            put("登封", new Province("Dengfeng 登封", 6951));
            put("德清", new Province("Deqing 德清", 7875));
            put("东莞", new Province("Dongguan 东莞", 11082));
            put("东营", new Province("Dongying 东营", 5322));
            put("都江堰", new Province("Dujianguan 都江堰", 10038));
            put("敦煌", new Province("Dunhuang 敦煌", 11166));
            put("峨眉山", new Province("Emeishan 峨眉山", 10644));
            put("佛山", new Province("Foshan 佛山", 11040));
            put("阜阳", new Province("Fuyang 阜阳", 6981));
            put("福州", new Province("Fuzhou 福州", 9963));
            put("赣州", new Province("Ganzhou 赣州", 9894));
            put("广州", new Province("Guanzhou 广州", 10965));
            put("桂林", new Province("Guilin 桂林", 10461));
            put("贵阳", new Province("Guiyang 贵阳", 10893));
            put("海口", new Province("Haikou 海口", 12543));
            put("海宁", new Province("Haining 海宁", 7968));
            put("哈密地", new Province("Hami 哈密地", 11097));
            put("杭州", new Province("Hangzhou 杭州", 7995));
            put("哈尔滨", new Province("Harbin 哈尔滨", 1461));
            put("合肥", new Province("Hefei 合肥", 7419));
            put("贺州", new Province("Hezhou 贺州", 10731));
            put("黄山", new Province("Huangshan 黄山", 8247));
            put("惠州", new Province("Huizhoy 惠州", 10740));
            put("湖州", new Province("Huzhou 湖州", 7761));
            put("吉安", new Province("Ji\"an 吉安", 9345));
            put("江门", new Province("Jiangmen 江门", 11274));
            put("嘉兴", new Province("Jiaxing 嘉兴", 7860));
            put("济南", new Province("Jinan 济南", 5616));
            put("景德镇", new Province("Jingdezhen 景德镇", 8412));
            put("靖江", new Province("Jingjiang 靖江", 7407));
            put("九寨沟", new Province("Jiuzhaigou 九寨沟", 9867));
            put("九江", new Province("Juijiang 九江", 8265));
            put("开封", new Province("Kaifeng 开封", 6573));
            put("昆明", new Province("Kunming 昆明", 12285));
            put("昆山", new Province("Kunshan 昆山", 7725));
            put("廊坊", new Province("Langfang 廊坊", 4704));
            put("兰州", new Province("Lanzhou 兰州", 9030));
            put("乐山", new Province("Leshan 乐山", 10560));
            put("丽江", new Province("Lijiang 丽江", 12525));
            put("凌海", new Province("Linhai 凌海", 3306));
            put("临沂", new Province("Linyi 临沂", 6135));
            put("溧阳", new Province("Liyang 溧阳", 7551));
            put("柳州", new Province("Luizhou 柳州", 10902));
            put("洛阳", new Province("Luoyang 洛阳", 7038));
            put("滿洲", new Province("Manchuria 滿洲", 4383));
            put("绵阳", new Province("Mianyang 绵阳", 9972));
            put("牡丹江", new Province("Mudanjiang 牡丹江", 507));
            put("南昌", new Province("Nangchang 南昌", 8694));
            put("南京", new Province("Nanjing 南京", 7317));
            put("南宁", new Province("Nanning 南宁", 11568));
            put("南通", new Province("Nantong 南通", 7497));
            put("南阳", new Province("Nanyang 南阳", 7392));
            put("宁波", new Province("Ningbo 宁波", 8355));
            put("宁德", new Province("Ningde 宁德", 9624));
            put("青岛", new Province("Qingdao 青岛", 6087));
            put("清远", new Province("Qinguan 清远", 10806));
            put("秦皇岛", new Province("Qinhuangdao 秦皇岛", 3936));
            put("泉州", new Province("Quanzhou 泉州", 10383));
            put("日照", new Province("Rizhao 日照", 6162));
            put("三亚", new Province("Sanya 三亚", 13398));
            put("上海市", new Province("Shanghai 上海市", 7854));
            put("香格里拉", new Province("Shangri-la 香格里拉", 13008));
            put("汕头", new Province("Shantou 汕头", 10764));
            put("韶关", new Province("Shaoguan 韶关", 10332));
            put("韶山", new Province("Shaoshan 韶山", 9168));
            put("沈阳", new Province("Shenyang 沈阳", 2730));
            put("深圳", new Province("Shenzhen 深圳", 11088));
            put("石家庄", new Province("Shijiazhuang 石家庄", 5514));
            put("石狮", new Province("Shishi 石狮", 10434));
            put("苏州", new Province("Suzhou 苏州", 7644));
            put("泰安", new Province("Taian 泰安", 5757));
            put("太原", new Province("Taiyuan 太原", 6147));
            put("台州", new Province("Taizhou 台州", 8766));
            put("唐山", new Province("Tangshan 唐山", 4302));
            put("天津", new Province("Tianjin 天津", 4686));
            put("万宁", new Province("Wanning 万宁", 13053));
            put("潍坊", new Province("Weifang 潍坊", 4704));
            put("威海", new Province("Weihai 威海", 6534));
            put("温州", new Province("Wenzhou 温州", 8898));
            put("乌海", new Province("Wuhai 乌海", 7635));
            put("武汉", new Province("Wuhan 武汉", 7989));
            put("芜湖", new Province("Wuhu 芜湖", 7698));
            put("无锡", new Province("Wuxi 无锡", 7599));
            put("乌镇", new Province("Wuzhen 乌镇", 6801));
            put("五指山", new Province("Wuzhishan 五指山", 13179));
            put("厦门市", new Province("Xiamen 厦门市", 10449));
            put("西安", new Province("Xian 西安", 7860));
            put("西昌", new Province("Xichang 西昌", 11307));
            put("西宁", new Province("Xining 西宁", 9624));
            put("新乡", new Province("Xinxiang 新乡", 6486));
            put("徐州", new Province("Xuzhou 徐州", 6483));
            put("雅安", new Province("Yaan 雅安", 10398));
            put("盐城", new Province("Yancheng 盐城", 7005));
            put("阳朔县", new Province("Yangshuo 阳朔县", 10683));
            put("扬州市", new Province("Yangzhou 扬州市", 7206));
            put("烟台", new Province("Yantai 烟台", 4110));
            put("宜昌", new Province("Yichang 宜昌", 8496));
            put("银川", new Province("Yinchuan 银川", 7971));
            put("义乌", new Province("Yiwu 义乌", 8430));
            put("岳阳", new Province("Yueyang 岳阳", 8628));
            put("张家界", new Province("Zhangjiajie 张家界", 9159));
            put("漳州", new Province("Zhangzhou 漳州", 10443));
            put("湛江", new Province("Zhanjiang 湛江", 11973));
            put("郑州", new Province("Zhengzhou 郑州", 6720));
            put("中山", new Province("Zhongshan 中山", 11250));
            put("舟山", new Province("Zhoushan 舟山", 8502));
            put("珠海", new Province("Zhuhai 珠海", 11352));
            put("淄博", new Province("Zibo 淄博", 5454));
            put("许昌", new Province("Xuchang 许昌", 6906));
        }
    };


    /**
     * одновалютный режим
     */
    public boolean isSingleCurrencyMode() {
        return currencies.size() == 1;
    }

    /**
     * валюта одновалютного режима.
     */
    public String singleCurrency() {
        return currencies.get(0);
    }


}
