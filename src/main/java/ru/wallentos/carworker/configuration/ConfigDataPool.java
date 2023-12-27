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
    @Value("${ru.wallentos.carworker.manager-logistics-spreedsheet-id:test}")
    public String managerLogisticsSpreedSheetId;
    @Value("${ru.wallentos.carworker.admin-list}")
    public List<String> adminList;
    @Value("${ru.wallentos.carworker.client-request-group-id}")
    public long clientRequestGroupId;
    @Value("${ru.wallentos.carworker.enable-client-request:false}")
    public boolean enableClientRequest;
    @Value("${ru.wallentos.carworker.korex-province-matrix:false}")
    public boolean isKorexProvinceMatrix;
    @Value("${ru.wallentos.carworker.is-cbr-rate-to-calculate:false}")
    public boolean isCbrRateToCalculate;
    @Value("${ru.wallentos.carworker.enable-result-detalization:false}")
    public boolean enableResultDetalization;
    @Value("${ru.wallentos.carworker.check-channel-subscribers:false}")
    public boolean isCheckChannelSubscribers;
    @Value("${ru.wallentos.carworker.channel-subscribers-id:-1002123611967}")
    public String channelSubscribersId;
    @Value("${ru.wallentos.carworker.channel-subscribers-link:link}")
    public String channelSubscribersLink;
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
    public static final String READY_BUTTON = "Готово";
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
    @Value("${ru.wallentos.carworker.enable-encar-report-mode:false}")
    public boolean enableEncarReportMode;
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
    public static Map<String, Double> conversionRatesMap;
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
    public static final String RESET_MANAGER_BUTTON = "Расчет стоимости Total to Ussuriysk/Bishkek";
    public static final String RESET_MESSAGE = "Рассчитать стоимость другого автомобиля";
    public static final String RESET_CALLBACK = "reset_callback";
    public static final String RESET_MANAGER_CALLBACK = "reset_manager_callback";
    public static final String TO_SET_CURRENCY_MENU = "Меню установки валюты";
    public static final String MANAGER_MESSAGE = "Связаться с менеджером";
    public static final String CAR_RESULT_DETAIL_BUTTON_TEXT = "Детализация расчёта";
    public static final String CAR_RESULT_DETAIL_BUTTON_CALLBACK = "result_details_callback";

    public static final String CAR_REPORT_BUTTON_TEXT = "Технический отчёт об автомобиле";
    public static final String CAR_REPORT_BUTTON_CALLBACK = "report_callback";
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
            put("安徽", new Province("Anhui 安徽", 7449, 0));
            put("甘肃", new Province("Gansu 甘肃", 9096, 0));
            put("吉林", new Province("Jilin 吉林", 1803, 0));
            put("广东", new Province("Guangdong 广东", 10998, 0));
            put("贵州", new Province("Guizhou 贵州", 10857, 0));
            put("辽宁", new Province("Liaoning 辽宁", 2673, 0));
            put("四川", new Province("Sichuan 四川", 10215, 0));
            put("福建", new Province("Fujian 福建", 9927, 0));
            put("海南", new Province("Hainan 海南", 12492, 0));
            put("湖北", new Province("Hubei 湖北", 8085, 0));
            put("湖南", new Province("Hunan 湖南", 9066, 0));
            put("河北", new Province("Hebei 河北", 5523, 0));
            put("河南", new Province("Henan 河南", 6669, 0));
            put("江西", new Province("Jiangxi 江西", 8643, 0));
            put("江苏", new Province("Jiangsu 江苏", 7296, 0));
            put("青海", new Province("Qinghai 青海", 9657, 0));
            put("山东", new Province("Shandong 山东", 5598, 0));
            put("山西", new Province("Shanxi 山西", 6096, 0));
            put("陕西", new Province("Shaanxi 陕西", 8088, 0));
            put("云南", new Province("Yunnan 云南", 12369, 0));
            put("北京", new Province("Beijing 北京", 4740, 0));
            put("上海市", new Province("Shanghai 上海市", 7875, 0));
            put("天津", new Province("Tianjin 天津", 9010, 0));
            put("大庆", new Province("Daqing 大庆", 1935, 0));
            put("哈尔滨", new Province("Harbin 哈尔滨", 1425, 0));
            put("牡丹江", new Province("Mudanjiang 牡丹江", 465, 0));
            put("常熟", new Province("Changshu 常熟", 8346, 0));
            put("常州", new Province("Changzhou 常州", 7479, 0));
            put("昆山", new Province("Kunshan 昆山", 7818, 0));
            put("溧阳", new Province("Liyang 溧阳", 7536, 0));
            put("南京", new Province("Nanjing 南京", 5771, 0));
            put("南通", new Province("Nantong 南通", 6991, 0));
            put("苏州", new Province("Suzhou 苏州", 7215, 0));
            put("台州", new Province("Taizhou 台州", 8005, 0));
            put("无锡", new Province("Wuxi 无锡", 7692, 0));
            put("大连", new Province("Dalian 大连", 3825, 0));
            put("徐州", new Province("Xuzhou 徐州", 8205, 0));
            put("盐城", new Province("Yancheng 盐城", 7367, 0));
            put("扬州市", new Province("Yangzhou 扬州市", 7221, 0));
            put("浙江", new Province("Zhejiang 浙江", 8070, 0));
            put("德清", new Province("Deqing 德清", 7968, 0));
            put("阜阳", new Province("Fuyang 阜阳", 7035, 0));
            put("海宁", new Province("Haining 海宁", 7998, 0));
            put("杭州", new Province("Hangzhou 杭州", 8067, 0));
            put("湖州", new Province("Huzhou 湖州", 7851, 0));
            put("嘉兴", new Province("Jiaxing 嘉兴", 7908, 0));
            put("凌海", new Province("Linhai 凌海", 5935, 0));
            put("宁波", new Province("Ningbo 宁波", 9814, 0));
            put("温州", new Province("Wenzhou 温州", 9562, 0));
            put("乌镇", new Province("Wuzhen 乌镇", 7920, 0));
            put("义乌", new Province("Yiwu 义乌", 8457, 0));
            put("舟山", new Province("Zhoushan 舟山", 8526, 0));
            put("长春", new Province("Changchun 长春", 5139, 0));
            put("鞍山", new Province("Anshan 鞍山", 3000, 0));
            put("丹东", new Province("Dandong 丹东", 2769, 0));
            put("沈阳", new Province("Shenyang 沈阳", 2679, 0));
            put("西宁", new Province("Xining 西宁", 9648, 0));
            put("敦煌", new Province("Dunhuang 敦煌", 11382, 0));
            put("兰州", new Province("Lanzhou 兰州", 9060, 0));
            put("西安", new Province("Xian 西安", 6600, 0));
            put("长治", new Province("Changzhi 长治", 6456, 0));
            put("大同", new Province("Datong 大同", 5742, 0));
            put("太原", new Province("Taiyuan 太原", 6132, 0));
            put("沧州", new Province("Cangzhou 沧州", 4986, 0));
            put("廊坊", new Province("Langfang 廊坊", 4746, 0));
            put("秦皇岛", new Province("Qinhuangdao 秦皇岛", 3897, 0));
            put("石家庄", new Province("Shijiazhuang 石家庄", 5535, 0));
            put("唐山", new Province("Tangshan 唐山", 6600, 0));
            put("成都", new Province("Chengdu 成都", 10068, 0));
            put("都江堰", new Province("Dujianguan 都江堰", 10110, 0));
            put("峨眉山", new Province("Emeishan 峨眉山", 10578, 0));
            put("九寨沟", new Province("Jiuzhaigou 九寨沟", 9942, 0));
            put("乐山", new Province("Leshan 乐山", 10485, 0));
            put("绵阳", new Province("Mianyang 绵阳", 9693, 0));
            put("西昌", new Province("Xichang 西昌", 11340, 0));
            put("保定", new Province("Baoding 保定", 5139, 0));
            put("武汉", new Province("Wuhan 武汉", 8094, 0));
            put("宜昌", new Province("Yichang 宜昌", 8529, 0));
            put("登封", new Province("Dengfeng 登封", 6969, 0));
            put("开封", new Province("Kaifeng 开封", 6591, 0));
            put("洛阳", new Province("Luoyang 洛阳", 7056, 0));
            put("南阳", new Province("Nanyang 南阳", 7458, 0));
            put("新乡", new Province("Xinxiang 新乡", 6501, 0));
            put("郑州", new Province("Zhengzhou 郑州", 6738, 0));
            put("东营", new Province("Dongying 东营", 5454, 0));
            put("济南", new Province("Jinan 济南", 5640, 0));
            put("临沂", new Province("Linyi 临沂", 6147, 0));
            put("青岛", new Province("Qingdao 青岛", 6210, 0));
            put("日照", new Province("Rizhao 日照", 6258, 0));
            put("泰安", new Province("Taian 泰安", 5784, 0));
            put("潍坊", new Province("Weifang 潍坊", 5796, 0));
            put("威海", new Province("Weihai 威海", 4380, 0));
            put("烟台", new Province("Yantai 烟台", 4365, 0));
            put("淄博", new Province("Zibo 淄博", 5502, 0));
            put("蚌埠", new Province("Bengbu 蚌埠", 7047, 0));
            put("巢湖", new Province("Chaohu 巢湖", 7545, 0));
            put("合肥", new Province("Hefei 合肥", 7440, 0));
            put("黄山", new Province("Huangshan 黄山", 8313, 0));
            put("芜湖", new Province("Wuhu 芜湖", 7614, 0));
            put("昆明", new Province("Kunming 昆明", 12405, 0));
            put("丽江", new Province("Lijiang 丽江", 12381, 0));
            put("香格里拉", new Province("Shangri-la 香格里拉", 14283, 0));
            put("贵阳", new Province("Guiyang 贵阳", 10857, 0));
            put("长沙", new Province("Changsha 长沙", 9069, 0));
            put("韶山", new Province("Shaoshan 韶山", 9192, 0));
            put("岳阳", new Province("Yueyang 岳阳", 8649, 0));
            put("张家界", new Province("Zhangjiajie 张家界", 9300, 0));
            put("赣州", new Province("Ganzhou 赣州", 9867, 0));
            put("吉安", new Province("Ji\"an 吉安", 9285, 0));
            put("景德镇", new Province("Jingdezhen 景德镇", 8436, 0));
            put("九江", new Province("Juijiang 九江", 8292, 0));
            put("南昌", new Province("Nangchang 南昌", 8646, 0));
            put("东莞", new Province("Dongguan 东莞", 11112, 0));
            put("佛山", new Province("Foshan 佛山", 11196, 0));
            put("广州", new Province("Guanzhou 广州", 10968, 0));
            put("惠州", new Province("Huizhoy 惠州", 10836, 0));
            put("江门", new Province("Jiangmen 江门", 11319, 0));
            put("清远", new Province("Qinguan 清远", 10884, 0));
            put("汕头", new Province("Shantou 汕头", 10794, 0));
            put("韶关", new Province("Shaoguan 韶关", 10371, 0));
            put("深圳", new Province("Shenzhen 深圳", 11160, 0));
            put("湛江", new Province("Zhanjiang 湛江", 12030, 0));
            put("中山", new Province("Zhongshan 中山", 7564, 0));
            put("珠海", new Province("Zhuhai 珠海", 11400, 0));
            put("福州", new Province("Fuzhou 福州", 9966, 0));
            put("靖江", new Province("Jingjiang 靖江", 10210, 0));
            put("龙阳君", new Province("Longyang 龙阳君", 10191, 0));
            put("宁德", new Province("Ningde 宁德", 9666, 0));
            put("泉州", new Province("Quanzhou 泉州", 10437, 0));
            put("石狮", new Province("Shishi 石狮", 10500, 0));
            put("厦门市", new Province("Xiamen 厦门市", 9138, 0));
            put("哈密地", new Province("Hami 哈密地", 11310, 0));
            put("赤峰", new Province("Chifeng 赤峰", 3675, 0));
            put("滿洲", new Province("Manchuria 滿洲", 4344, 0));
            put("乌海", new Province("Wuhai 乌海", 7842, 0));
            put("北海", new Province("Beihai 北海", 12012, 0));
            put("桂林", new Province("Guilin 桂林", 10488, 0));
            put("贺州", new Province("Hezhou 贺州", 10124, 0));
            put("柳州", new Province("Luizhou 柳州", 10938, 0));
            put("南宁", new Province("Nanning 南宁", 15767, 0));
            put("阳朔县", new Province("Yangshuo 阳朔县", 10710, 0));
            put("银川", new Province("Yinchuan 银川", 8301, 0));
            put("重庆", new Province("Chongqing 重庆", 7713, 0));
            put("儋州", new Province("Danzhou 儋州", 9735, 0));
            put("海口", new Province("Haikou 海口", 8937, 0));
            put("三亚", new Province("Sanya 三亚", 13347, 0));
            put("万宁", new Province("Wanning 万宁", 12993, 0));
            put("五指山", new Province("Wuzhishan 五指山", 13128, 0));
        }
    };


    /**
     * Карта китайских провинций и соответствующих им ценников, для расчёта по ссылке.
     * Имя провинции - наценка в ЮАНях.
     */
    public static final Map<String, Province> provincePriceMapForKorex = new LinkedHashMap<>() {
        {
            put("鞍山", new Province("Anshan 鞍山", 3006, 0));
            put("保定", new Province("Baoding 保定", 5121, 0));
            put("北海", new Province("Beihai 北海", 12066, 0));
            put("北京", new Province("Beijing 北京", 4725, 0));
            put("蚌埠", new Province("Bengbu 蚌埠", 7032, 0));
            put("沧州", new Province("Cangzhou 沧州", 4965, 0));
            put("长春", new Province("Changchun 长春", 1836, 0));
            put("长沙", new Province("Changsha 长沙", 9048, 0));
            put("常熟", new Province("Changshu 常熟", 7650, 0));
            put("长治", new Province("Changzhi 长治", 6420, 0));
            put("常州", new Province("Changzhou 常州", 7383, 0));
            put("巢湖", new Province("Chaohu 巢湖", 7509, 0));
            put("成都", new Province("Chengdu 成都", 10053, 0));
            put("赤峰", new Province("Chifeng 赤峰", 3687, 0));
            put("重庆", new Province("Chongqing 重庆", 9924, 0));
            put("大连", new Province("Dalian 大连", 3612, 0));
            put("丹东", new Province("Dandong 丹东", 2775, 0));
            put("儋州", new Province("Danzhou 儋州", 12819, 0));
            put("大庆", new Province("Daqing 大庆", 1941, 0));
            put("大同", new Province("Datong 大同", 5616, 0));
            put("登封", new Province("Dengfeng 登封", 6951, 0));
            put("德清", new Province("Deqing 德清", 7875, 0));
            put("东莞", new Province("Dongguan 东莞", 11082, 0));
            put("东营", new Province("Dongying 东营", 5322, 0));
            put("都江堰", new Province("Dujianguan 都江堰", 10038, 0));
            put("敦煌", new Province("Dunhuang 敦煌", 11166, 0));
            put("峨眉山", new Province("Emeishan 峨眉山", 10644, 0));
            put("佛山", new Province("Foshan 佛山", 11040, 0));
            put("阜阳", new Province("Fuyang 阜阳", 6981, 0));
            put("福州", new Province("Fuzhou 福州", 9963, 0));
            put("赣州", new Province("Ganzhou 赣州", 9894, 0));
            put("广州", new Province("Guanzhou 广州", 10965, 0));
            put("桂林", new Province("Guilin 桂林", 10461, 0));
            put("贵阳", new Province("Guiyang 贵阳", 10893, 0));
            put("海口", new Province("Haikou 海口", 12543, 0));
            put("海宁", new Province("Haining 海宁", 7968, 0));
            put("哈密地", new Province("Hami 哈密地", 11097, 0));
            put("杭州", new Province("Hangzhou 杭州", 7995, 0));
            put("哈尔滨", new Province("Harbin 哈尔滨", 1461, 0));
            put("合肥", new Province("Hefei 合肥", 7419, 0));
            put("贺州", new Province("Hezhou 贺州", 10731, 0));
            put("黄山", new Province("Huangshan 黄山", 8247, 0));
            put("惠州", new Province("Huizhoy 惠州", 10740, 0));
            put("湖州", new Province("Huzhou 湖州", 7761, 0));
            put("吉安", new Province("Ji\"an 吉安", 9345, 0));
            put("江门", new Province("Jiangmen 江门", 11274, 0));
            put("嘉兴", new Province("Jiaxing 嘉兴", 7860, 0));
            put("济南", new Province("Jinan 济南", 5616, 0));
            put("景德镇", new Province("Jingdezhen 景德镇", 8412, 0));
            put("靖江", new Province("Jingjiang 靖江", 7407, 0));
            put("九寨沟", new Province("Jiuzhaigou 九寨沟", 9867, 0));
            put("九江", new Province("Juijiang 九江", 8265, 0));
            put("开封", new Province("Kaifeng 开封", 6573, 0));
            put("昆明", new Province("Kunming 昆明", 12285, 0));
            put("昆山", new Province("Kunshan 昆山", 7725, 0));
            put("廊坊", new Province("Langfang 廊坊", 4704, 0));
            put("兰州", new Province("Lanzhou 兰州", 9030, 0));
            put("乐山", new Province("Leshan 乐山", 10560, 0));
            put("丽江", new Province("Lijiang 丽江", 12525, 0));
            put("凌海", new Province("Linhai 凌海", 3306, 0));
            put("临沂", new Province("Linyi 临沂", 6135, 0));
            put("溧阳", new Province("Liyang 溧阳", 7551, 0));
            put("柳州", new Province("Luizhou 柳州", 10902, 0));
            put("洛阳", new Province("Luoyang 洛阳", 7038, 0));
            put("滿洲", new Province("Manchuria 滿洲", 4383, 0));
            put("绵阳", new Province("Mianyang 绵阳", 9972, 0));
            put("牡丹江", new Province("Mudanjiang 牡丹江", 507, 0));
            put("南昌", new Province("Nangchang 南昌", 8694, 0));
            put("南京", new Province("Nanjing 南京", 7317, 0));
            put("南宁", new Province("Nanning 南宁", 11568, 0));
            put("南通", new Province("Nantong 南通", 7497, 0));
            put("南阳", new Province("Nanyang 南阳", 7392, 0));
            put("宁波", new Province("Ningbo 宁波", 8355, 0));
            put("宁德", new Province("Ningde 宁德", 9624, 0));
            put("青岛", new Province("Qingdao 青岛", 6087, 0));
            put("清远", new Province("Qinguan 清远", 10806, 0));
            put("秦皇岛", new Province("Qinhuangdao 秦皇岛", 3936, 0));
            put("泉州", new Province("Quanzhou 泉州", 10383, 0));
            put("日照", new Province("Rizhao 日照", 6162, 0));
            put("三亚", new Province("Sanya 三亚", 13398, 0));
            put("上海市", new Province("Shanghai 上海市", 7854, 0));
            put("香格里拉", new Province("Shangri-la 香格里拉", 13008, 0));
            put("汕头", new Province("Shantou 汕头", 10764, 0));
            put("韶关", new Province("Shaoguan 韶关", 10332, 0));
            put("韶山", new Province("Shaoshan 韶山", 9168, 0));
            put("沈阳", new Province("Shenyang 沈阳", 2730, 0));
            put("深圳", new Province("Shenzhen 深圳", 11088, 0));
            put("石家庄", new Province("Shijiazhuang 石家庄", 5514, 0));
            put("石狮", new Province("Shishi 石狮", 10434, 0));
            put("苏州", new Province("Suzhou 苏州", 7644, 0));
            put("泰安", new Province("Taian 泰安", 5757, 0));
            put("太原", new Province("Taiyuan 太原", 6147, 0));
            put("台州", new Province("Taizhou 台州", 8766, 0));
            put("唐山", new Province("Tangshan 唐山", 4302, 0));
            put("天津", new Province("Tianjin 天津", 4686, 0));
            put("万宁", new Province("Wanning 万宁", 13053, 0));
            put("潍坊", new Province("Weifang 潍坊", 4704, 0));
            put("威海", new Province("Weihai 威海", 6534, 0));
            put("温州", new Province("Wenzhou 温州", 8898, 0));
            put("乌海", new Province("Wuhai 乌海", 7635, 0));
            put("武汉", new Province("Wuhan 武汉", 7989, 0));
            put("芜湖", new Province("Wuhu 芜湖", 7698, 0));
            put("无锡", new Province("Wuxi 无锡", 7599, 0));
            put("乌镇", new Province("Wuzhen 乌镇", 6801, 0));
            put("五指山", new Province("Wuzhishan 五指山", 13179, 0));
            put("厦门市", new Province("Xiamen 厦门市", 10449, 0));
            put("西安", new Province("Xian 西安", 7860, 0));
            put("西昌", new Province("Xichang 西昌", 11307, 0));
            put("西宁", new Province("Xining 西宁", 9624, 0));
            put("新乡", new Province("Xinxiang 新乡", 6486, 0));
            put("徐州", new Province("Xuzhou 徐州", 6483, 0));
            put("雅安", new Province("Yaan 雅安", 10398, 0));
            put("盐城", new Province("Yancheng 盐城", 7005, 0));
            put("阳朔县", new Province("Yangshuo 阳朔县", 10683, 0));
            put("扬州市", new Province("Yangzhou 扬州市", 7206, 0));
            put("烟台", new Province("Yantai 烟台", 4110, 0));
            put("宜昌", new Province("Yichang 宜昌", 8496, 0));
            put("银川", new Province("Yinchuan 银川", 7971, 0));
            put("义乌", new Province("Yiwu 义乌", 8430, 0));
            put("岳阳", new Province("Yueyang 岳阳", 8628, 0));
            put("张家界", new Province("Zhangjiajie 张家界", 9159, 0));
            put("漳州", new Province("Zhangzhou 漳州", 10443, 0));
            put("湛江", new Province("Zhanjiang 湛江", 11973, 0));
            put("郑州", new Province("Zhengzhou 郑州", 6720, 0));
            put("中山", new Province("Zhongshan 中山", 11250, 0));
            put("舟山", new Province("Zhoushan 舟山", 8502, 0));
            put("珠海", new Province("Zhuhai 珠海", 11352, 0));
            put("淄博", new Province("Zibo 淄博", 5454, 0));
            put("许昌", new Province("Xuchang 许昌", 6906, 0));
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
