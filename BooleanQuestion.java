package apptesting;

import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class BooleanQuestion extends Question {
	private int defaultResponse = 1;
	
	//Constructor without custom default response
	public BooleanQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses) {
		super(driver, sheet, text, column, mode, numResponses);
	}
	
	//Constructor with custom default response
	public BooleanQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses, int defaultResponse) {
		super(driver, sheet, text, column, mode, numResponses);
		this.defaultResponse = defaultResponse;
	}
	
	//Implementation of test method from parent class
	public void test(int option, int position) {
		List<WebElement> options = getDriver().findElements(By.xpath(getXpath(position) + "//label"));
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
	
	//Clicks given option and records in given XSSFSheet
	private void click(WebElement option) {
		String text = option.getText();
		Cell cell = getSheet().getRow(getSheet().getLastRowNum()).createCell(getColumn());
		cell.setCellValue(text);	
		option.click();
	}
}
