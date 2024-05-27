package test;


import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import utils.TaskQueue;
import utils.OutputWriter;
import utils.URLConstants;
import utils.WindowManager;

import java.io.*;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class LoginTest {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 500;
    private static final int ROWS = 2;
    private static final int COLS = 8;

    private static WindowManager windowManager = new WindowManager(SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, ROWS, COLS);

    public LoginTest() {
        System.setProperty("webdriver.chrome.driver", "F:\\Downloads\\chromedriver-win64\\chromedriver.exe");
    }


    public void login(WebDriver driver, String username, String password) throws InterruptedException {
        driver.get(URLConstants.LOGIN_URL);
        WebElement usernameInput = driver.findElement(By.id("username"));
        WebElement passwordInput = driver.findElement(By.id("password"));
        WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));

        usernameInput.sendKeys(username);
        passwordInput.sendKeys(password);
        submitButton.click();

        Thread.sleep(2000);

        if (driver.getCurrentUrl().contains(URLConstants.LOGIN_URL)) {
            throw new RuntimeException("Đăng nhập thất bại");
        }
    }

    public String loginTest(String username, String password) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        windowManager.acquire();
        windowManager.setWindowPositionAndSize(driver);
        try {
            driver.get(URLConstants.LOGIN_URL);
            WebElement usernameInput = driver.findElement(By.id("username"));
            if (!username.equals("null")) {
                usernameInput.sendKeys(username);
            }

            WebElement passwordInput = driver.findElement(By.id("password"));
            if (!password.equals("null")) {
                passwordInput.sendKeys(password);
            }
            WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
            submitButton.click();

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            String currentUrl = driver.getCurrentUrl();
            if (!currentUrl.contains(URLConstants.LOGIN_URL)) {
                return "Đăng nhập thành công";
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
                return "Unknown error";
            }
        } finally {
            driver.quit();
            windowManager.release();
        }
    }

    public void performLoginFromCSV(String csvFilePath) throws IOException {
        TaskQueue cl = new TaskQueue();
        int numThreads = cl.countLines(csvFilePath);
        ExecutorService executorService = Executors.newFixedThreadPool(numThreads);
        List<Future<String>> futures = new ArrayList<>();
        List<String> lines = new ArrayList<>();

        try (InputStream is = getClass().getClassLoader().getResourceAsStream(csvFilePath);
             BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            String line;
            while ((line = br.readLine()) != null) {
                String[] data = line.split(",");
                String username = data[0];
                String password = data[1];
                Future<String> future = executorService.submit(() -> {
                    try {
                        return loginTest(username, password);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        return "Error";
                    }
                });
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
                String updatedLine = data[0] + "," + data[1] + "," + result;
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
