package ru.wallentos.carworker.service;

import com.twocaptcha.TwoCaptcha;
import com.twocaptcha.captcha.ReCaptcha;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Service;

@Service
public class RecaptchaService {

    private String captchaApiKey;
    private UtilService utilService;

    public RecaptchaService(UtilService utilService) {
        this.utilService = utilService;
    }

    public void solveReCaptcha(String url, Document document) {
        String stringToParse =
                document.select("script[type=text/javascript]").get(1).childNodes().get(0).attributes().get("#data").lines().filter(s -> s.contains("grecaptcha.execute")).findFirst().orElse("");
        String sitekey = utilService.parseCaptchaKey(stringToParse);
        String action = utilService.parseCaptchaAction(stringToParse);
        System.out.println("siteKey is " + sitekey + " action is " + action);
        TwoCaptcha solver = new TwoCaptcha(captchaApiKey);
        ReCaptcha captcha = new ReCaptcha();
        captcha.setSiteKey(sitekey);
        captcha.setUrl(url);
        captcha.setVersion("v3");
        captcha.setAction(action);
        captcha.setScore(0.3);
        try {
            solver.solve(captcha);
            System.out.println("Captcha solved: " + captcha.getCode());
            System.out.println("remains in cents: " + solver.balance()*100);
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

//http://2captcha.com/in.php?key=1abc234de56fab7c89012d34e56fa7b8&method=userrecaptcha&version=v3&action=verify&min_score=0.3
//&googlekey=6LfZil0UAAAAAAdm1Dpzsw9q0F11-bmervx9g5fE&pageurl=http://mysite.com/page/
    }

    public void solveReCaptchaDemo() {
        TwoCaptcha solver = new TwoCaptcha(captchaApiKey);
        ReCaptcha captcha = new ReCaptcha();
        captcha.setSiteKey("6LfB5_IbAAAAAMCtsjEHEHKqcB9iQocwwxTiihJu");
        captcha.setUrl("https://2captcha.com/demo/recaptcha-v3");
        captcha.setVersion("v3");
        captcha.setAction("demo_action");
        captcha.setScore(0.9);
        try {
            solver.solve(captcha);
            System.out.println("Captcha solved: " + captcha.getCode());
        } catch (Exception e) {
            System.out.println("Error occurred: " + e.getMessage());
        }

//http://2captcha.com/in.php?key=1abc234de56fab7c89012d34e56fa7b8&method=userrecaptcha&version=v3&action=verify&min_score=0.3
//&googlekey=6LfZil0UAAAAAAdm1Dpzsw9q0F11-bmervx9g5fE&pageurl=http://mysite.com/page/
    }
}
