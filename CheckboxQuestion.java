package apptesting;

import java.util.*;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.ss.usermodel.Cell;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.JavascriptExecutor;

public class CheckboxQuestion extends Question {
	private int[] defaultResponse = {0};
	
	//Constructor without custom default response
	public CheckboxQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses) {
		super(driver, sheet, text, column, mode, numResponses);
	}
	
	//Constructor with custom default response
	public CheckboxQuestion(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numResponses, int[] defaultResponse) {
		super(driver, sheet, text, column, mode, numResponses);
		this.defaultResponse = defaultResponse;
	}
	
	//Implementation of test method from parent class
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
			Cell cell = getSheet().getRow(getSheet().getLastRowNum()).createCell(getColumn());
			
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", options.get(defaultResponse[0]));
			String s = options.get(defaultResponse[0]).getText();
			for (int i = 1; i < defaultResponse.length; i++) {
				((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", options.get(defaultResponse[i]));
				s += ", " + options.get(defaultResponse[i]).getText();
			}
			cell.setCellValue(s);
		}
	}
	
	//Clicks given option and records in given XSSFSheet
	private void click(WebElement option) {
		Cell cell = getSheet().getRow(getSheet().getLastRowNum()).createCell(getColumn());
		cell.setCellValue(option.getText());
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].click();", option);
	}
}

