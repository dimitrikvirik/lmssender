package git.dimitrikvirik.lmssender;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.mail.internet.MimeMessage;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class GPAListenner {

    private final JavaMailSender mailSender;

    ChromeDriver chromeDriver;
    Set<StudyItem> savedStudyItems = new HashSet<>();

    @Value("${GMAIL_EMAIL}")
    private String gmailEmail;

    @Value("${LMS_USER}")
    private String lmsUser;

    @Value("${LMS_PASSWORD}")
    private String lmsPassword;

    @Value("${CHROME_DRIVER}")
    private String chromeDriverPath;


    void registreDriver() {
        System.setProperty("webdriver.chrome.driver", chromeDriverPath);
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        chromeDriver = new ChromeDriver(options);


    }


    @PostConstruct
    void loginUser() throws InterruptedException {
        registreDriver();
        chromeDriver.get("https://lms.tsu.ge");
        chromeDriver.findElement(By.id("UserName")).sendKeys(lmsUser); // lms username
        chromeDriver.findElement(By.id("Password")).sendKeys(lmsPassword); // lms password
        chromeDriver.findElement(By.id("login")).click();
        String title = chromeDriver.getTitle();
        Thread.sleep(1000);
        checkChanges();
    }

    void checkChanges() throws InterruptedException {
        while (true) {
            log.info("Checking LMS Student card " + LocalDateTime.now());
            chromeDriver.get("https://lms.tsu.ge/StudentCard");
            Thread.sleep(1000);
            List<WebElement> elements = chromeDriver.findElements(By.className("x-grid-row"));
            elements.forEach(WebElement::click);
            List<WebElement> rows = new ArrayList<>();
            while (rows.isEmpty()) {
                Thread.sleep(500);
                rows = chromeDriver.findElements(By.className("x-grid-cell-inner"));
            }

            Set<StudyItem> studyItems = new HashSet<>();
            for (int i = 0; i < rows.size(); i += 10) {
                StudyItem studyItem = new StudyItem();

                studyItem.setType(rows.get(i + 1).getText());
                studyItem.setProfessor(rows.get(i + 2).getText());
                studyItem.setName(rows.get(i + 3).getText());
                studyItem.setSelectType(rows.get(i + 4).getText());
                studyItem.setCredit(Integer.valueOf(rows.get(i + 5).getText()));
                studyItem.setScore(Integer.valueOf(rows.get(i + 6).getText()));
                studyItem.setScoreComponent(rows.get(i + 7).getText());
                studyItems.add(studyItem);
            }
            if (savedStudyItems.isEmpty()) {
                savedStudyItems.addAll(studyItems);
            } else {
                studyItems.removeAll(savedStudyItems);

                if (!studyItems.isEmpty()) {
                    String collect = studyItems.stream().map(StudyItem::toString).collect(Collectors.joining("<br><hr>"));
                    sendMail(collect);
                }
            }
            Thread.sleep(TimeUnit.HOURS.toMillis(1));
        }
    }

    void sendMail(String content) {
        String subject = "LMS Student Card";
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");
            helper.setTo(gmailEmail);
            helper.setFrom(gmailEmail);
            helper.setText(content, true);
            helper.setSubject(subject);
            mailSender.send(message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
