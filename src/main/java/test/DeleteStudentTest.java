package test;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import utils.URLConstants;
import utils.WindowManager;

import java.util.List;

public class DeleteStudentTest {
    private static final int SCREEN_WIDTH = 1920;
    private static final int SCREEN_HEIGHT = 1080;
    private static final int WINDOW_WIDTH = 200;
    private static final int WINDOW_HEIGHT = 500;
    private static final int ROWS = 2;
    private static final int COLS = 8;

    private static WindowManager windowManager = new WindowManager(SCREEN_WIDTH, SCREEN_HEIGHT, WINDOW_WIDTH, WINDOW_HEIGHT, ROWS, COLS);

    public DeleteStudentTest() {
        System.setProperty("webdriver.chrome.driver", "F:\\Downloads\\chromedriver-win64\\chromedriver.exe");
    }

    public String deleteStudentTest(String username, String password, String studentId) throws InterruptedException {
        WebDriver driver = new ChromeDriver();
        windowManager.acquire();
        windowManager.setWindowPositionAndSize(driver);
        boolean studentFound = false;
        try{
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

                        Thread.sleep(1000);

                        WebElement deleteOption = menu.findElement(By.xpath(".//a[text()='Xoá']"));
                        deleteOption.click();
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
        }
        finally {
            driver.quit();
        }
    }
}
