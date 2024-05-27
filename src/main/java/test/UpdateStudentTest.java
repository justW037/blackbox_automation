package test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.OutputWriter;
import utils.TaskQueue;
import utils.URLConstants;
import utils.WindowManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UpdateStudentTest {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 500;
    private static final int ROWS = 2;
    private static final int COLS = 13;

    private static WindowManager windowManager = new WindowManager(SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, ROWS, COLS);

    public UpdateStudentTest() {
        System.setProperty("webdriver.chrome.driver", "F:\\Downloads\\chromedriver-win64\\chromedriver.exe");
    }

    public String updateStudentTest(String username, String password, String studentId, String studentName, String email, String phone, String birthday, String address, String sex) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        windowManager.acquire();
        windowManager.setWindowPositionAndSize(driver);
        boolean studentFound = false;

        try {
            LoginTest loginTest = new LoginTest();
            loginTest.login(driver, username, password);
            driver.get(URLConstants.HOME_URL);

            List<WebElement> paginationLinks = driver.findElements(By.cssSelector(".pagination a.active"));

            for (int pageIndex = 0; pageIndex < paginationLinks.size(); pageIndex++) {
                if (pageIndex > 0) {
                    paginationLinks.get(pageIndex).click();
                    Thread.sleep(1000);
                }

                List<WebElement> rows = driver.findElements(By.cssSelector(".student-table tbody tr"));

                for (WebElement row : rows) {
                    WebElement idCell = row.findElement(By.cssSelector("td:nth-child(1)"));

                    if (idCell.getText().equals(studentId)) {
                        studentFound = true;
                        WebElement menuButton = row.findElement(By.id("btn-menu"));
                        menuButton.click();
                        WebElement menu = row.findElement(By.id("menu"));

                        if (menu == null || !menu.isDisplayed()) {
                            menuButton.click();
                        }

                        WebElement editOption = menu.findElement(By.xpath(".//li[text()='Sửa']"));
                        editOption.click();
                        WebElement editForm = driver.findElement(By.id("edit-form"));

                        if (editForm == null || !editForm.isDisplayed()) {
                            editOption.click();
                        }

                        Thread.sleep(1000);

                        WebElement studentNameInput = editForm.findElement(By.name("name"));
                        studentNameInput.clear();
                        if (!studentName.equals("null")) {
                            studentNameInput.sendKeys(studentName);
                        }

                        WebElement emailInput = editForm.findElement(By.name("email"));
                        emailInput.clear();
                        if (!email.equals("null")) {
                            emailInput.sendKeys(email);
                        }

                        WebElement phoneInput = editForm.findElement(By.name("phone"));
                        phoneInput.clear();
                        if (!phone.equals("null")) {
                            phoneInput.sendKeys(phone);
                        }

                        WebElement birthdayInput = editForm.findElement(By.name("birthday"));
                        birthdayInput.clear();
                        if (!birthday.equals("null")) {
                            birthdayInput.sendKeys(birthday);
                        }

                        WebElement addressInput = editForm.findElement(By.name("address"));
                        addressInput.clear();
                        if (!address.equals("null")) {
                            addressInput.sendKeys(address);
                        }

                        WebElement sexInput = sex.equals("0") ? editForm.findElement(By.id("male")) : editForm.findElement(By.id("female"));
                        if (!sex.equals("null")) {
                            sexInput.click();
                        }

                        WebElement submitButton = editForm.findElement(By.id("udt-submit"));
                        submitButton.click();

                        Thread.sleep(2000);

                        try {
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
                        } catch (Exception ea) {
                            try {
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
                            } catch (Exception e) {
                                return "Sửa sinh viên thành công";
                            }
                        }
                        break;
                    }
                }

                if (studentFound) {
                    break;
                }

                paginationLinks = driver.findElements(By.cssSelector(".pagination a.active"));
            }

            if (!studentFound) {
                return "Không tìm thấy sinh viên";
            }

            return "Unknown error";
        } finally {
            driver.quit();
        }
    }


    public void performUpdateStudentFromCSV(String csvFilePath) throws IOException {
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
                String studentId = data[6];
                Future<String> future = executorService.submit(() -> updateStudentTest(username, password, studentId ,studentName, email, phone, birthday, address, sex));
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
                String updatedLine = data[0] + "," + data[1] + "," + data[2] + "," + data[3] + "," + data[4] + "," + data[5] + "," + data[6] + "," + result;
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
