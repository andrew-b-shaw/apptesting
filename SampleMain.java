package apptesting;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;

import java.io.*;
import java.util.concurrent.TimeUnit;

/*
 * A sample program utilizing the other classes to test an AfterPattern app. Sensitive information
 * has been redacted and in future iterations would be stored externally using salt and hash.
 */
public class SampleMain {
    public static final String PROJECT_URL = "";
    public static final String APP_NAME = "";
    public static final String FILE_PATH = "";
    public static final String SHEET_NAME = "";
    public static final String USERNAME = "";
    public static final String PASSWORD = "";

    //Main method
    public static void main(String[] args) throws FileNotFoundException, IOException {
        XSSFWorkbook workbook = getWorkbook(FILE_PATH);
        WebDriver driver = new ChromeDriver();
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        login(driver);

        App app = new App(driver, APP_NAME);
        app.importFromExcel(workbook, SHEET_NAME);
        app.test();

        FileOutputStream output = new FileOutputStream(FILE_PATH);
        workbook.write(output);
        output.close();
        driver.close();
        System.out.println("Program complete!");
    }

    //Import Excel spreadsheet workbook object
    public static XSSFWorkbook getWorkbook(String file) throws FileNotFoundException, IOException {
        FileInputStream input = new FileInputStream(new File(file));
        XSSFWorkbook workbook = new XSSFWorkbook(input);
        workbook.createSheet(SHEET_NAME);
        return workbook;
    }

    //Login to Afterpattern and close cookies notice
    public static void login(WebDriver driver) {
        driver.get("");
        driver.findElement(By.id("user_email")).sendKeys(USERNAME);
        driver.findElement(By.id("user_password")).sendKeys(PASSWORD);
        driver.findElement(By.className("blue-btn")).click();

        driver.get(PROJECT_URL);
    }
}
