package apptesting;

import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.openqa.selenium.WebDriver;

/*
 * The abstract Question class represents questions in the AfterPattern web interface.
 * It contains methods to interact with and gather information on each question.
 */
abstract class Question {
	private WebDriver driver;
	private XSSFSheet sheet;
	private String text;
	private int column;
	private int numOptions;
	private String mode;
	private boolean lastOption;
	
	/*
	 * Creates a new Question object from the given driver, XSSFSheet, question text, corresponding column
	 * on Excel sheet, testing mode, and number of options
	 * @param driver - the Selenium WebDriver to use
	 * @param sheet - the XSSFSheet object to print to
	 * @param text - the question text as a String
	 * @param column - the column index of the XSSFSheet to print to as an integer
	 * @param mode - the testing mode as a String
	 * @param numOptions - the number of options as an integer
	 */
	protected Question(WebDriver driver, XSSFSheet sheet, String text, int column, String mode, int numOptions) {
		this.driver = driver;
		this.sheet = sheet;
		this.text = text;
		this.column = column;
		this.mode = mode;
		this.numOptions = numOptions;
		lastOption = true;
	}
	
	/*
	 * @return - corresponding WebDriver being used
	 */
	public WebDriver getDriver() {
		return driver;
	}
	
	/*
	 * @return - corresponding XSSFSheet to print to
	 */
	public XSSFSheet getSheet() {
		return sheet;
	}
	
	/*
	 * @return - question text
	 */
	public String getText() {
		return text;
	}
	
	/*
	 * @return - column index for excel sheet
	 */
	public int getColumn() {
		return column;
	}
	
	/*
	 * @return - number of options
	 */
	public int getNumOptions() {
		return numOptions;
	}
	
	/*
	 * Return - testing type (default, random, test)
	 */
	public String getMode() {
		return mode;
	}
	
	/*
	 * @return - question xpath
	 */
	public String getXpath(int i) {
		return "(//div[@class='page-block__field-input'])[" + (i + 1) + "]";
	}
	
	/*
	 * Sets lastOption variable
	 * @param b - whether the current option is the question's last option
	 */
	protected void setLastOption(boolean b) {
		lastOption = b;
	}
	
	/*
	 * @return - true if the last selected option was the last option
	 */
	public boolean isLastOption() {
		return lastOption;
	}

	/*
	 * Abstract method called test implemented by all subclasses
	 * Selects given option and prints the selection to Excel
	 * @param option - the integer index of the option to select (0 index)
	 * @param position - the question's position on the page (0 index)
	 */
	abstract public void test(int option, int position);
}
