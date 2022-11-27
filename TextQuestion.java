package apptesting;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class TextQuestion extends Question {
	private String defaultResponse = "123";
	
	//Constructor without custom default response
	public TextQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses) {
		super(driver, sheet, text, column, mode, numResponses);
	}
	
	//Constructor with custom default response
	public TextQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses, String defaultResponse) {
		super(driver, sheet, text, column, mode, numResponses);
		this.defaultResponse = defaultResponse;
	}
	
	//Implementation of test method from parent class
	public void test(int option, int i) {
		Cell cell = getSheet().getRow(getSheet().getLastRowNum()).createCell(getColumn());
		cell.setCellValue(defaultResponse);
		getDriver().findElement(By.xpath(getXpath(i) + "//input")).sendKeys(defaultResponse);
	}
}
