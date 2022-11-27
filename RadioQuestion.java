package apptesting;

import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

/*
 * The RadioQuestion class is an implementation of the abstract Question class.
 * It contains methods to interact with radio questions in the AfterPattern web interface
 */
public class RadioQuestion extends Question {
	private int defaultResponse = 1;
	
	/*
	 * Creates a new Question object from the given driver, XSSFSheet, question text, corresponding column
	 * on Excel sheet, testing mode, and number of options without custom default response
	 * @param driver - the Selenium WebDriver to use
	 * @param sheet - the XSSFSheet object to print to
	 * @param text - the question text as a String
	 * @param column - the column index of the XSSFSheet to print to as an integer
	 * @param mode - the testing mode as a String
	 * @param numOptions - the number of options as an integer
	 */
	public RadioQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses) {
		super(driver, sheet, text, column, mode, numResponses);
	}
	
	/*
	 * Creates a new Question object from the given driver, XSSFSheet, question text, corresponding column
	 * on Excel sheet, testing mode, and number of options with custom default response
	 * @param driver - the Selenium WebDriver to use
	 * @param sheet - the XSSFSheet object to print to
	 * @param text - the question text as a String
	 * @param column - the column index of the XSSFSheet to print to as an integer
	 * @param mode - the testing mode as a String
	 * @param numOptions - the number of options as an integer
	 * @param defaultResponse - the custom default response as a String
	 */
	public RadioQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses, int defaultResponse) {
		super(driver, sheet, text, column, mode, numResponses);
		this.defaultResponse = defaultResponse;
	}
	
	/*
	 * Implementation of abstract method test from parent class
	 * Selects given option and prints the selection to Excel
	 * @param option - the integer index of the option to select (0 index)
	 * @param position - the question's position on the page (0 index)
	 */
	public void test(int option, int position) {
		List<WebElement> options = getDriver().findElements(By.xpath(getXpath(position) + "//p"));
		switch (getMode()) {
		case "test":
			click(options.get(option));
			setLastOption(option == options.size() - 1);
			break;
		case "random":
			Random r = new Random();
			click(options.get(r.nextInt(options.size())));
			break;
		default:
			click(options.get(defaultResponse));
		}
	}
	
	/*
	 * Clicks given option and records in given XSSFSheet
	 * @param option - the integer index of the option to select
	 */
	private void click(WebElement option) {
		String text = option.getText();
		Cell cell = getSheet().getRow(getSheet().getLastRowNum()).createCell(getColumn());
		cell.setCellValue(text);
		option.click();
	}
}
