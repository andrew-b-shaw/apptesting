package apptesting;

import java.util.*;
import java.util.concurrent.TimeUnit;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/*
 * The App class controls the test runs of the AfterPattern app. It reads and prints data
 * to an Excel sheet. The user can change the testing conditions by manipulating the Excel sheet.
 * It can also adaptively add new questions it encounters even if not provided by the user initially.
 * Users can set how many iterations of options they want to test and whether they want to test
 * options randomly, sequentially, or always select the first option. The App class interacts
 * with the AfterPattern app through the Selenium API.
 */
public class SampleApp {
    private WebDriver driver;
    private String xpath;
    private XSSFSheet sheet;
    private String originalURL;
    private HashMap<String, Question> questions = new HashMap<String, Question>();
    private HashMap<String, Integer> responses = new HashMap<String, Integer>();
    private ArrayList<Question> testQuestions = new ArrayList<Question>();
    private ArrayList<String> results = new ArrayList<String>();

    /*
     * Constructs a new App object with the given WebDriver and app name
     * @param driver - the Selenium WebDriver corresponding to the window the AfterPattern page is in
     * @param appName - the name of the AfterPattern app as a String
     */
    public App(WebDriver driver, String appName) {
        this.driver = driver;
        xpath = "//h4[text()='" + appName + "']/../../..//a";
    }

    /*
     * Gets question info from excel spreadsheet and sets up output sheet
     * @param workbook - the XSSFWorkbook object to retrieve from and print to
     * @param sheetName - the name of the Excel sheet to retrieve from as a String
     */
    public void importFromExcel(XSSFWorkbook workbook, String sheetName) {
        sheet = workbook.getSheet(sheetName);
        sheet.createRow(0);
        XSSFSheet inputSheet = workbook.getSheet("Questions");
        int size = inputSheet.getLastRowNum() - inputSheet.getFirstRowNum();

        //Iterate over rows in input sheet and store values in arrays
        DataFormatter df = new DataFormatter();
        for (int i = 0; i < size; i++) {
            Row inputRow = inputSheet.getRow(i + 1);
            String text = inputRow.getCell(0).getStringCellValue();
            String mode = inputRow.getCell(1).getStringCellValue();
            String type = inputRow.getCell(2).getStringCellValue();
            int numOptions = (int) inputRow.getCell(3).getNumericCellValue();
            String defaultResponse = df.formatCellValue(inputRow.getCell(4));

            //Create a question without a custom default response if the default response value is -1,
            //otherwise create a question with a custom default response
            if (defaultResponse.equals("-1")) {
                createQuestion(type, text, mode, numOptions);
            } else {
                createQuestion(type, text, mode, numOptions, defaultResponse);
            }
        }
        sheet.createRow(1);
    }

    /*
     * Runs app and selects response options for each run. The number of runs is equal to the product of
     * the number of options for each question with testing mode "test"
     */
    public void test() {
        test(0);
        printResults();
    }

    /*
     * Recursively runs app and sets response option for each run.
     * @param i - the index of the current question on "test" mode being iterated over
     */
    private void test(int i) {
        Question q = testQuestions.get(i);
        int option = 0;
        do {
            responses.replace(q.getText(), option++);
            if (i < testQuestions.size() - 1) {
                test(i + 1);
            } else {
                runApp();
            }

            if (q.isLastOption()) {
                break;
            }
        } while (option < q.getNumOptions());
    }

    /*
     * Runs app from AfterPattern project folder
     */
    public void runApp() {
        driver.findElement(By.xpath(xpath)).click();

        //Switch tab focus
        String current = driver.getWindowHandle();
        Set<String> handles = driver.getWindowHandles();
        for (String s : handles) {
            if (!s.equals(current)) {
                driver.switchTo().window(s);
            }
        }

        originalURL = driver.getCurrentUrl();

        //Call test method of next Question and create new Question if necessary
        int i = 0;
        while (!endCondition()) {
            //Test if next question is in stored map of questions by question text
            List<WebElement> page = driver.findElements(By.className("page-block__field-label"));
            WebElement currentQuestion = page.get(i);
            String text = currentQuestion.findElement(By.xpath(".//p")).getText();
            if (!questions.containsKey(text)) {
                createQuestion(currentQuestion);
            }

            //Call test method
            questions.get(text).test(responses.get(text), i);

            //Click continue if last question on page
            page = driver.findElements(By.className("page-block__field-label"));
            if (i >= page.size() - 1) {
                clickContinue();
                i = 0;
            } else {
                i++;
            }
        }
        end();
    }

    /*
     * Clicks continue button if the driver is on the last question on the page
     * or if the next page has no questions on it
     */
    public void clickContinue() {
        //Initial click
        driver.findElement(By.className("btn-primary")).click();
        driver.findElement(By.xpath("//*[@class!='disabled' and @class='btn-primary']"));

        //Further clicks if no questions are on the next page
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
        List<WebElement> questionsOnPage = driver.findElements(By.className("page-block__field-label"));

        while (questionsOnPage.size() == 0) {
            try {
                driver.findElement(By.className("btn-primary")).click();
                driver.findElement(By.className("btn-primary"));
                driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
                driver.findElement(By.xpath("//*[@class!='disabled' and @class='btn-primary']"));
                driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
                questionsOnPage = driver.findElements(By.className("page-block__field-label"));
            } catch (org.openqa.selenium.NoSuchElementException e) {
                break;
            }
        }
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    /*
     * Creates new Question object without custom default response
     * @param type - the type of the question created as a String
     * @param text - the text of the question as a String
     * @param mode - the testing mode of the question as a String
     * @param numOptions - the number of options the question has as an integer
     */
    private void createQuestion(String type, String text, String mode, int numOptions) {
        Question q;
        switch (type) {
            case "dropdown":
                q = new DropdownQuestion(driver, sheet, text, questions.size(), mode, numOptions);
                break;
            case "radio":
                q = new RadioQuestion(driver, sheet,  text, questions.size(), mode, numOptions);
                break;
            case "boolean":
                q = new BooleanQuestion(driver, sheet, text, questions.size(), mode, numOptions);
                break;
            case "checkbox":
                q = new CheckboxQuestion(driver, sheet, text, questions.size(), mode, numOptions);
                break;
            default:
                q = new TextQuestion(driver, sheet, text, questions.size(), mode, numOptions);
        }

        sheet.getRow(0).createCell(questions.size()).setCellValue(text);
        questions.put(text, q);
        responses.put(text, 0);
        if (mode.equals("test")) {
            testQuestions.add(q);
        }
    }

    /*
     * Creates new Question object with custom default response
     * @param type - the type of the question created as a String
     * @param text - the text of the question as a String
     * @param mode - the testing mode of the question as a String
     * @param numOptions - the number of options the question has as an integer
     * @param defaultResponse - the custom default response as a String
     */
    private void createQuestion(String type, String text, String mode, int numOptions, String defaultResponse) {
        Question q;
        switch (type) {
            case "dropdown":
                q = new DropdownQuestion(driver, sheet, text, questions.size(), mode, numOptions, (int) Double.parseDouble(defaultResponse));
                break;
            case "radio":
                q = new RadioQuestion(driver, sheet, text, questions.size(), mode, numOptions, (int) Double.parseDouble(defaultResponse));
                break;
            case "boolean":
                q = new BooleanQuestion(driver, sheet, text, questions.size(), mode, numOptions, (int) Double.parseDouble(defaultResponse));
                break;
            case "checkbox":
                q = new CheckboxQuestion(driver, sheet, text, questions.size(), mode, numOptions, stringToIntArray(defaultResponse));
                break;
            default:
                q = new TextQuestion(driver, sheet, text, questions.size(), mode, numOptions, defaultResponse);
        }

        sheet.getRow(0).createCell(questions.size()).setCellValue(text);
        questions.put(text, q);
        responses.put(text, 0);
        if (mode.equals("test")) {
            testQuestions.add(q);
        }
    }

    /*
     * Adaptively creates new Question object from WebElement on current page
     * @param q - the WebElement corresponding to the question to be created
     */
    public void createQuestion(WebElement q) {
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
        String type;

        //Detect question type for creating new question
        try {
            type = q.findElement(By.xpath("./..//fieldset")).getAttribute("class");
            if (type.contains("radio")) {
                type = "radio";
            } else if (type.contains("boolean")){
                type = "boolean";
            } else {
                type = "checkbox";
            }
        } catch (org.openqa.selenium.NoSuchElementException e) {
            try {
                q.findElement(By.xpath("./..//select"));
                type = "dropdown";
            } catch (org.openqa.selenium.NoSuchElementException f) {
                type = "text";
            }
        }

        createQuestion(type, q.findElement(By.xpath(".//p")).getText(), "default", 0);
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
    }

    /*
     * Sets end condition (the signal for when the apps should check for the success condition)
     */
    private boolean endCondition() {
        //End condition: the page has switched to a different url
        return !driver.getCurrentUrl().equals(originalURL);
    }

    /*
     * Returns app to first page and sets success condition
     */
    public void end() {
        Row row = sheet.getRow(sheet.getLastRowNum());
        String state = row.getCell(0).getStringCellValue();
        sheet.createRow(sheet.getLastRowNum() + 1);

        //Success condition: the number of elements containing the [success condition] is greater than 0
        driver.manage().timeouts().implicitlyWait(10, TimeUnit.MILLISECONDS);
        List<WebElement> search = driver.findElements(By.xpath("//*[contains()]"));
        driver.manage().timeouts().implicitlyWait(30, TimeUnit.SECONDS);
        boolean success = search.size() > 0;
        if (success) {
            results.add("Success!");
        } else {
            results.add("Failure");
        }

        //Close app tab and switch to project tab
        Set<String> handles = driver.getWindowHandles();
        String current = driver.getWindowHandle();
        String original = "";
        for (String s : handles) {
            if (!s.equals(current)) {
                original = s;
            }
        }
        driver.close();
        driver.switchTo().window(original);
    }

    /*
     * Prints results to excel sheet
     */
    private void printResults() {
        Row row = sheet.getRow(0);
        int cellNum = row.getLastCellNum();
        row.createCell(cellNum).setCellValue("Results");
        for (int i = 1; i < sheet.getLastRowNum(); i++) {
            sheet.getRow(i).createCell(cellNum).setCellValue(results.get(i - 1));
        }
    }


    /*
     * Converst String to int array for checkbox custom default response
     * @param s - the String to be converted to an array
     */
    private int[] stringToIntArray(String s) {
        //Get length of int array and create it
        Scanner scanner = new Scanner(s);
        int length = 0;
        while (scanner.hasNext()) {
            scanner.next();
            length++;
        }
        int[] array = new int[length];

        //Import values into int array
        scanner.close();
        scanner = new Scanner(s);
        for (int i = 0; i < length; i++) {
            array[i] = Integer.parseInt(scanner.next().replaceAll(",", ""));
        }
        scanner.close();
        return array;
    }
}