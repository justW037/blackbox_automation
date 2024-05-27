package test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import utils.TaskQueue;
import utils.OutputWriter;
import utils.URLConstants;
import utils.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RegisterTest {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WINDOW_WIDTH = 150;
    private static final int WINDOW_HEIGHT = 400;
    private static final int ROWS = 3;
    private static final int COLS = 9;

    private static WindowManager windowManager = new WindowManager(SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, ROWS, COLS);

    public RegisterTest() {
        System.setProperty("webdriver.chrome.driver", "F:\\Downloads\\chromedriver-win64\\chromedriver.exe");
    }

    public String registerTest(String username, String password, String retypePassword, String email) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        windowManager.acquire();
        windowManager.setWindowPositionAndSize(driver);
        try {
            driver.get(URLConstants.REGISTER_URL);
            WebElement usernameInput = driver.findElement(By.id("username"));
            if (!username.equals("null")) {
                usernameInput.sendKeys(username);
            }

            WebElement passwordInput = driver.findElement(By.id("password"));
            if (!password.equals("null")) {
                passwordInput.sendKeys(password);
            }

            WebElement retypePasswordInput = driver.findElement(By.id("retypePassword"));
            if (!retypePassword.equals("null")) {
                retypePasswordInput.sendKeys(retypePassword);
            }

            WebElement emailInput = driver.findElement(By.id("email"));
            if (!email.equals("null")) {
                emailInput.sendKeys(email);
            }

            WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
            submitButton.click();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains(URLConstants.REGISTER_URL)) {
                return "Đăng ký thành công";
            } else {
                WebElement errorMessageElement = driver.findElement(By.id("errorMessage"));
                if (!errorMessageElement.getText().isEmpty()) {
                    String errorMessage = errorMessageElement.getText();
                    return errorMessage;
                }
                if (!passwordInput.getAttribute("validationMessage").isEmpty()) {
                    return passwordInput.getAttribute("validationMessage");
                }
                if (!usernameInput.getAttribute("validationMessage").isEmpty()) {
                    return usernameInput.getAttribute("validationMessage");
                }
                if (!retypePasswordInput.getAttribute("validationMessage").isEmpty()) {
                    return retypePasswordInput.getAttribute("validationMessage");
                }
                if (!emailInput.getAttribute("validationMessage").isEmpty()) {
                    return emailInput.getAttribute("validationMessage");
                }
                return "Unknown error";
            }
        } finally {
            driver.quit();
            windowManager.release();
        }
    }

    public void performRegisterFromCSV(String csvFilePath) throws IOException {
        TaskQueue taskQueue = new TaskQueue();
        int numThreads = taskQueue.countLines(csvFilePath);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<String> lines = new ArrayList<>();
        List<Future<String>> futures = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(csvFilePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String username = data[0];
                String password = data[1];
                String retypePassword = data[2];
                String email = data[3];

                Future<String> future = executorService.submit(() -> registerTest(username, password, retypePassword, email));
                futures.add(future);
                lines.add(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> updatedLines = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            try {
                String result = futures.get(i).get();
                String[] data = lines.get(i).split(",");
                String updatedLine = data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + result;
                updatedLines.add(updatedLine);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        OutputWriter op = new OutputWriter();
        op.writeUpdatedLinesToFile(csvFilePath, updatedLines);

        executorService.shutdown();
    }
}