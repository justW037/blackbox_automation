package test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import utils.OutputWriter;
import utils.TaskQueue;
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

public class AddStudentTest {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 500;
    private static final int ROWS = 2;
    private static final int COLS = 13;

    private static WindowManager windowManager = new WindowManager(SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, ROWS, COLS);

    public AddStudentTest() {
        System.setProperty("webdriver.chrome.driver", "F:\\Downloads\\chromedriver-win64\\chromedriver.exe");
    }

    public String addStudentTest(String username, String password, String studentName, String email, String phone, String birthday, String address, String sex) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        windowManager.acquire();
        windowManager.setWindowPositionAndSize(driver);
        try {
            LoginTest loginTest = new LoginTest();
            loginTest.login(driver, username, password);
            driver.get(URLConstants.HOME_URL);

            WebElement addButton = driver.findElement(By.cssSelector(".add-btn"));
            addButton.click();
            Thread.sleep(1000);

            WebElement addForm = driver.findElement(By.id("add-form"));
            if (addForm == null || !addForm.isDisplayed()){
                addButton.click();
            }

            Thread.sleep(2000);


            WebElement studentNameInput = driver.findElement(By.name("name"));
            if(!studentName.equals("null")){
                studentNameInput.sendKeys(studentName);
            }

            WebElement emailInput = driver.findElement(By.name("email"));
            if(!email.equals("null")){
                emailInput.sendKeys(email);
            }

            WebElement phoneInput = driver.findElement(By.name("phone"));
            if(!phone.equals("null")){
                phoneInput.sendKeys(phone);
            }

            WebElement birthdayInput = driver.findElement(By.name("birthday"));
            if(!birthday.equals("null")){
                birthdayInput.sendKeys(birthday);
            }

            WebElement addressInput = driver.findElement(By.name("address"));
            if(!address.equals("null")){
                addressInput.sendKeys(address);
            }

            WebElement sexInput = sex.equals("0") ? driver.findElement(By.id("male")) : driver.findElement(By.id("female"));
            if(!sex.equals("null")){
                sexInput.click();
            }

            WebElement submitButton = driver.findElement(By.xpath("//button[@type='submit']"));
            submitButton.click();

            Thread.sleep(2000);


            try{
                if (!studentNameInput.getAttribute("validationMessage").isEmpty()) {
                    return studentNameInput.getAttribute("validationMessage");
                }
                if (!emailInput.getAttribute("validationMessage").isEmpty()) {
                    return emailInput.getAttribute("validationMessage");
                }
                if (!phoneInput.getAttribute("validationMessage").isEmpty()) {
                    return phoneInput.getAttribute("validationMessage");
                }
                if (!birthdayInput.getAttribute("validationMessage").isEmpty()) {
                    return birthdayInput.getAttribute("validationMessage");
                }
                if (!addressInput.getAttribute("validationMessage").isEmpty()) {
                    return addressInput.getAttribute("validationMessage");
                }

            }
            catch (Exception ea){
                try{
                    WebElement errorMessageElement = driver.findElement(By.id("errorMessage"));
                    List<WebElement> errorMessages = errorMessageElement.findElements(By.cssSelector("ul li"));
                    StringBuilder errorMessage = new StringBuilder();
                    for (WebElement errorMessageItem : errorMessages) {
                        if (!errorMessageItem.getText().isEmpty()) {
                            if (errorMessage.length() > 0) {
                                errorMessage.append("; ");
                            }
                            errorMessage.append(errorMessageItem.getText());
                        }
                    }
                    if (errorMessage.length() > 0) {
                        return errorMessage.toString();
                    }
                } catch(Exception e) {
                    return "Thêm sinh viên thành công";
                }
            }



            return "Unknown error";
        } finally {
            driver.quit();
            windowManager.release();
        }
    }

    public void performAddStudentFromCSV(String csvFilePath) throws IOException {
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
                String username = "123123";
                String password = "12345678";
                String studentName = data[0];
                String email = data[1];
                String phone = data[2];
                String birthday = data[3];
                String address = data[4];
                String sex = data[5];

                Future<String> future = executorService.submit(() -> addStudentTest(username, password, studentName, email, phone, birthday, address, sex));
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
                String updatedLine = data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + result;
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
