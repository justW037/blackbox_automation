package utils;

import org.openqa.selenium.Dimension;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;

import java.util.concurrent.Semaphore;

public class WindowManager {
    private int screenWidth;
    private int screenHeight;
    private int windowWidth;
    private int windowHeight;
    private int rows;
    private int cols;
    private int currentRow = 0;
    private int currentCol = 0;
    private Semaphore semaphore;

    public WindowManager(int screenWidth, int screenHeight, int windowWidth, int windowHeight, int rows, int cols) {
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.windowWidth = windowWidth;
        this.windowHeight = windowHeight;
        this.rows = rows;
        this.cols = cols;
        this.semaphore = new Semaphore(rows * cols);
    }

    public synchronized void setWindowPositionAndSize(WebDriver driver) {
        if (currentRow >= rows) {
            throw new RuntimeException("Not enough screen space to arrange windows without overlap");
        }

        int x = currentCol * windowWidth;
        int y = currentRow * windowHeight;

        driver.manage().window().setSize(new Dimension(windowWidth, windowHeight));
        driver.manage().window().setPosition(new Point(x, y));

        currentCol++;
        if (currentCol >= cols) {
            currentCol = 0;
            currentRow++;
        }
    }

    public void acquire() throws InterruptedException {
        semaphore.acquire();
    }

    public void release() {
        semaphore.release();
        synchronized (this) {
            if (currentRow > 0 && currentCol == 0) {
                currentRow--;
                currentCol = cols - 1;
            } else if (currentCol > 0) {
                currentCol--;
            }
        }
    }
}