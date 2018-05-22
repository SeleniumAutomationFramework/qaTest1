package com.objectbrains.qa;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Properties;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.NoSuchWindowException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Action;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import net.serenitybdd.core.pages.PageObject;
public class AbstractPage extends PageObject{
	private final long MAX_DEFAULT_TIMEOUT = 300;
	public static final String JENKINS_CONFIG_FILENAME = "jenkins.config.properties";
	public static final String KEY_PROJECT_NAME = "test.projectname";
	public static final String KEY_DEFAULT_TIMEOUT = "test.defaulttimeout";	
	public final long defaultTimeout = loadDefaultTimeout(KEY_DEFAULT_TIMEOUT, JENKINS_CONFIG_FILENAME, MAX_DEFAULT_TIMEOUT);		
	
	private final String PROJECT_NAME = readValueFromPropertiesFile(KEY_PROJECT_NAME, JENKINS_CONFIG_FILENAME);	
	private final boolean isP2Cucumber = PROJECT_NAME.equalsIgnoreCase("P2");
	private final boolean isP1Cucumber = PROJECT_NAME.equalsIgnoreCase("P1");

	By toastErrorMessage = By.cssSelector("div.toast-error");
	By toastSuccessMessage = By.cssSelector("div.toast-success");
	By busyloadAnimation = By.cssSelector("div.cg-busy.cg-busy-animation");
	
	protected WebDriverWait wait;
	

	public AbstractPage() {
		super();
	}
	
	private long loadDefaultTimeout(String key, String filename, long maxValue) {
		long timeout = 0;
		try {
		    String value = readValueFromPropertiesFile(key, filename);
			timeout = Long.parseLong(value);
			if (timeout > maxValue) {
				timeout = maxValue;
			}			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return timeout;		
	}
	
	public static String readValueFromPropertiesFile(String key, String filename){
		String value = "";
		Properties props = null;
		File file = new File(filename);
		try {
			FileReader reader = new FileReader(file);
			props = new Properties();
			props.load(reader);
			String result =  props.getProperty(key);
			reader.close();
			if (result != null && !result.isEmpty()) {
				value = result.trim();
			}
		}catch(IOException ex) {
			ex.printStackTrace();
		}	
		return value;			
	}

	/**
	 * Accepts an alert message pop up
	 * 
	 */
	public void acceptAlertMessage(){
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		wait.until(ExpectedConditions.alertIsPresent());
		Alert alert = getDriver().switchTo().alert();
		alert.accept();
	}

	/**
	 * Click using Actions
	 */
	public void actionClick(By bySelector) {
		WebElement myDynamicElement = new WebDriverWait(getDriver(), defaultTimeout)
				.until(ExpectedConditions.elementToBeClickable(bySelector));
		Actions action = new Actions(getDriver());
		action.click(myDynamicElement).build().perform();
	}

	/**
	 * Captures screenshot of page and a png
	 * 
	 * @param driver
	 *            WebDriver
	 * @param folder
	 *            string of relative path to directory screenshots should be
	 *            stored in
	 * @param fileName
	 *            name of the file without extension
	 * @return Returns file path where file is located
	 */
	public String captureScreenshot(WebDriver driver, String folder, String fileName) {
		WebDriver augmentedDriver = new Augmenter().augment(driver);
		File screenshotFile = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
		File targetFile = new File(folder, fileName + ".png");
		try {
			FileUtils.copyFile(screenshotFile, targetFile);
		} catch (IOException e) {
			System.out.println("Error while writing file");
		}
		return targetFile.getAbsolutePath();
	}

	/**
	 * Returns true if an element is displayed on the page
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return boolean value if element is displayed on the page
	 */
	public boolean checkElementDisplayed(By bySelector) {
		WebElement tempElement = getDriver().findElement(bySelector);
		return checkElementDisplayed(tempElement);
	}

	public boolean checkElementDisplayed(WebElement webElement) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.visibilityOf(webElement));
		boolean result = myDynamicElement.isDisplayed();
		return result;
	}

	/**
	 * Returns boolean indicating whether or not an element is displayed on the
	 * page
	 * 
	 * @param bySelector
	 *            By selector of element being interacted with
	 * @return Returns true of an element is on the page
	 */
	public boolean checkIfElementIsOnPage(By bySelector) {
		try {
			getDriver().findElement(bySelector);
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}

	/**
	 * Returns boolean indicating whether or not an error message is displayed
	 * on P2 Servicing web site
	 * 
	 * @return Returns true of the error message toast is shown
	 */
	public boolean checkIfThereIsErrorMessage() {
		try {
			getDriver().findElement(toastErrorMessage);
			System.out.println(getTextOfElement(toastErrorMessage));
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}
	
	/**
	 * Waits for the page to finish loading and checks if an error message appears
	 */
	public void verifyNoErrorMessage(){
		sleep(500);
		waitForLoading();
		sleep(500);
		if (checkIfThereIsErrorMessage()) {
			String errorMessage = returnErrorMessage();
			Assert.fail(errorMessage);
		}
	}	
	

	/**
	 * Returns boolean indicating whether or not a success message is displayed
	 * on the P2 Servicing web site
	 * 
	 * @return Returns true of there is a success message
	 */
	public boolean checkIfThereIsSuccessMessage() {
		try {
			getDriver().findElement(toastSuccessMessage);
		} catch (NoSuchElementException e) {
			return false;
		}
		return true;
	}

	public boolean checkIfWindowExists(String title) {
		String current = getDriver().getTitle();
		if (switchToWindow(title)) {
			switchToWindow(current);
			return true;
		}
		return false;
	}

	/**
	 * Waits for a WebElement to be present on the page and then clears the
	 * contents of that WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void clearElement(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.clear();
	}
	
	@Deprecated
	public void clickLoanDataGrid(WebDriver driver){
		// click on data on grid
		waitForLoading();
		waitClick(By.id("trSearchResult_0"));
	}
	
	/**
	 * Waits for WebElement defined by the bySelector parameter to be visible
	 * before attempting to click on it
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void clickWhenVisible(By bySelector) {
		clickWhenVisible(bySelector, defaultTimeout);
	}

	/**
	 * Waits for WebElement defined by the bySelector parameter to be visible
	 * before attempting to click on it
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param timeout
	 *            Time which WebDriver should wait
	 */
	public void clickWhenVisible(By bySelector, long timeout) {
		WebElement myDynamicElement = new WebDriverWait(getDriver(), timeout)
				.until(ExpectedConditions.visibilityOfElementLocated(bySelector));
		myDynamicElement.click();
	}

	public void closeCurrentWindow() {
		getDriver().close();
	}

	public void closeWindow(String title) {
		try {
			if (switchToWindow(title)) {
				this.closeCurrentWindow();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	@Deprecated
	public void closeApplication() {
		this.getDriver().close();
	}		
	
	public void deleteAllCookies() {
		getDriver().manage().deleteAllCookies();;
	}	
	
	public void maximizeWindow() {
		getDriver().manage().window().maximize();
	}
	
	public void setWindowPosition(Point targetPosition) {
		getDriver().manage().window().setPosition(targetPosition);;
	}
	
	public void quitDriver() {
		getDriver().quit();
	}
	
	public void closeDriver() {
		getDriver().close(); 
	}
	

	/**
	 * Compares data in two tables, each table is represented as a list of list
	 * of strings.
	 * 
	 * @param firstTable
	 *             the first table to compare
	 * @param secondTable
	 *            the second table to compare
	 * @return <code>true</code> if both tables are the same; <code>false</code>
	 *         otherwise.
	 */
	public boolean compareDataOfTables(List<List<String>> firstTable, List<List<String>> secondTable) {
		boolean result = true;
		if ((firstTable == null) || (secondTable == null)) {
			if ((firstTable == null) && (secondTable == null)) {
				return true;
			} else if ((firstTable == null)) {
				if (secondTable.isEmpty()) {
					return true;
				} else {
					return false;
				}
			} else {
				if (firstTable.isEmpty()) {
					return true;
				} else {
					return false;
				}
			}
		}
		if ((firstTable.isEmpty()) || (secondTable.isEmpty())) {
			if ((firstTable.isEmpty()) && (secondTable.isEmpty())) {
				return true;
			} else {
				return false;
			}
		}
		// Compare rows:
		int firstTableRows = firstTable.size();
		int secondTableRows = secondTable.size();
		if (firstTableRows != secondTableRows) {
			System.out.println("Number of rows is different: " + firstTableRows + ", " + secondTableRows);
			return false;
		}
		// Compare columns:
		int firstTableColumns = firstTable.get(0).size();
		int secondTableColumns = secondTable.get(0).size();
		if (firstTableColumns != secondTableColumns) {
			System.out.println("Number of columns is different: " + firstTableColumns + ", " + secondTableColumns);
			return false;
		}
		// Compare the Values
		int counter = 0;
		for (int i = 0; i < firstTableRows; i++) {
			for (int j = 0; j < firstTableColumns; j++) {
				String firstTableValue = firstTable.get(i).get(j).trim();
				String secondTableValue = secondTable.get(i).get(j).trim();
				if (!firstTableValue.equalsIgnoreCase(secondTableValue)) {
					counter++;
					result = false;
					System.out.println("Value is different: " + firstTableValue + ", " + secondTableValue);
				}
			}
		}
		if (!result) {
			System.out.println("Number of different values: " + counter);
			return false;
		}
		return true;
	}

	/**
	 * Returns true if specified text if it can be found in the given WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param text
	 *            Searches for this text in the given WebElement
	 * @return Returns true if specified text if it can be found in the given
	 *         WebElement
	 */
	public Boolean containsVisibleText(By bySelector, String text) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		Boolean elementText = wait.until(ExpectedConditions.textToBePresentInElementLocated(bySelector, text));
		return elementText;
	}
	
	/**
	 * Downloads a file from a given URL, Currently only works with websites
	 * which do not require file permissions
	 * 
	 * @param url
	 *            url where the file is
	 * @param filePathToDownloadTo
	 *            String of where to store file
	 * @throws IOException
	 *             IOException
	 */
	
	/*
	public void downloadFileFromUrl(URL url, String filePathToDownloadTo) throws IOException {
		DownloadFileFromUrl downloadFileFromUrl = new DownloadFileFromUrl();
		downloadFileFromUrl.downloadFileFromUrl(url, filePathToDownloadTo);
	}
	
	*/

	/**
	 * Returns WebElement when it becomes visible on the page
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return Returns WebElement when it becomes visible on the page
	 */
	public WebElement elementVisibilityOf(By bySelector) {
		WebElement tempElement = getDriver().findElement(bySelector);
		WebElement myDynamicElement = wait.until(ExpectedConditions.visibilityOf(tempElement));
		return myDynamicElement;
	}

	/**
	 * Date picker, changes day only
	 * 
	 * @param bySelector the By selector of the date picker
	 * @param value the day to pick
	 */
	public void enterDayFromDatePicker(By bySelector, String value) {
		WebElement dateDatePicker = getDriver().findElement(bySelector);
		List<WebElement> dayCells = dateDatePicker.findElements(By.tagName("td"));
		for (WebElement dayCell : dayCells) {
			if (!isDisplayedAndIsEnabled(dayCell)){
				continue;
			}
			String day = dayCell.getText();
			if (day.equals(value)) {
				try{
					WebElement link = dayCell.findElement(By.linkText(value));
					link.click();
					break;
				}catch(NoSuchElementException ex){
					continue;
				}
			}
		}
	}

	/**
	 * Scrolls around page until it finds an element in the display port
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return true if the element is on the page
	 */
	public boolean findElementOnPage(By bySelector) {
		int windowHeight = getDriver().manage().window().getSize().getHeight();
		int previousHeightValue = 0;
		int currentHeightValue = getDriver().manage().window().getPosition().getY();
		boolean elementIsVisible = false;
		int counter = 0;
		while (((currentHeightValue - previousHeightValue) > windowHeight) || (counter == 0)) {
			previousHeightValue = currentHeightValue;
			scrollPageLengthDown();
			currentHeightValue = getDriver().manage().window().getPosition().getY();
			elementIsVisible = getDriver().findElement(bySelector).isDisplayed();
			counter++;
			if (elementIsVisible) {
				break;
			}
		}
		return elementIsVisible;
	}

	/**
	 * Finds how many occurrences of a value in the table
	 * 
	 * @param dataTable
	 *            the table to search for a
	 *            value
	 * @param value the string to find
	 * @param ignoreCase
	 *             if case is ignored; <code>false</code>
	 *            otherwise.
	 * @return number of occurrences of a value in the table
	 */
	public int findValueInDataTable(List<List<String>> dataTable, String value, boolean ignoreCase) {

		if (dataTable.isEmpty()) {
			return 0;
		}
		int dataTableRows = dataTable.size();
		int dataTableColumns = dataTable.get(0).size();
		// Search the value
		int counter = 0;
		for (int i = 0; i < dataTableRows; i++) {
			for (int j = 0; j < dataTableColumns; j++) {
				String tableValue = dataTable.get(i).get(j).trim();
				if (ignoreCase) {
					if (value.equalsIgnoreCase(tableValue)) {
						counter++;
					}
				} else {
					if (value.equals(tableValue)) {
						counter++;
					}
				}
			}
		}
		return counter;
	}

	/**
	 * Returns the attribute value of an element peter.dinh
	 * 
	 * @param bySelector
	 * @param attributeName
	 * @return
	 */
	public String getAttributeValueOfElement(By bySelector, String attributeName) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement tempElement = getDriver().findElement(bySelector);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement = wait.until(ExpectedConditions.visibilityOf(tempElement));
		String attributeText = myDynamicElement.getAttribute(attributeName);
		return attributeText;
	}
		
	public String getAttributeValue(WebElement element, String attributeName) {
		String attributeText = element.getAttribute(attributeName);
		return attributeText;
	}	
		
	public String getAttributeValue(By bySelector, String attributeName) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		String attributeText = getAttributeValue(myDynamicElement, attributeName);
		return attributeText;
	}	
	
	public void setAttributeValue(WebElement element, String attributeName, String attributeValue) {
		((JavascriptExecutor) getDriver()).executeScript("arguments[0].setAttribute('" + attributeName + "',arguments[1]);", element, attributeValue);
	}	
		
	public void setAttributeValue(By bySelector, String attributeName, String attributeValue) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		setAttributeValue(myDynamicElement, attributeName, attributeValue);
	}	

	/**
	 * Returns title of current page
	 */
	public String getCurrentPageTitle() {
		return getDriver().getTitle();
	}

	/**
	 * Gets data from a web table in the web page and stores in a list of list
	 * of strings. Assumes that the first row is always a header row and there
	 * is no merged cells in the table.
	 * 
	 * @param bySelector
	 *            By selector of the web table element.
	 * @param includeHeader
	 *            <code>true</code> If table header is included;
	 *            <code>false</code> otherwise.
	 * @return all data in table's cells are stored in a list of list of
	 *         strings.
	 */
	public List<List<String>> getDataFromWebTable(By bySelector, boolean includeHeader) {
		List<List<String>> dataTable = new ArrayList<List<String>>();
		WebElement webTable = this.getDriver().findElement(bySelector);
		List<WebElement> listOfRowElements = webTable.findElements(By.tagName("tr"));
		if ((listOfRowElements == null) || (listOfRowElements.size() == 0)
				|| (!includeHeader && listOfRowElements.size() == 1)) {
			return dataTable;
		}
		// Extract data from web table element and fill the data table:
		boolean isHeaderRow = true;
		List<WebElement> listOfElementsInRow = null;
		for (WebElement rowElement : listOfRowElements) {
			if (!includeHeader) {
				if (isHeaderRow) {
					isHeaderRow = false;
					continue;
				} else {
					listOfElementsInRow = rowElement.findElements(By.tagName("td"));
				}
			} else {
				if (isHeaderRow) {
					isHeaderRow = false;
					listOfElementsInRow = rowElement.findElements(By.tagName("th"));
				} else {
					listOfElementsInRow = rowElement.findElements(By.tagName("td"));
				}
			}

			List<String> myRow = new ArrayList<String>();
			for (WebElement element : listOfElementsInRow) {
				myRow.add(element.getText());
			}
			dataTable.add(myRow);
		}
		int columnCount = dataTable.get(0).size();
		int rowCount = dataTable.size();
		System.out.println("Rows: " + rowCount + ", Columns: " + columnCount);
		return dataTable;
	}

	public WebElement getElementFromDataRow(WebElement rowElement, int columnIndex) {
		List<WebElement> listOfElementsInRow = rowElement.findElements(By.tagName("td"));
		int elementCount = listOfElementsInRow.size();
		WebElement result = null;
		for (int i = 0; i < elementCount; i++) {
			if (columnIndex == i) {
				result = listOfElementsInRow.get(i);
				break;
			}
		}
		return result;
	}
	
	public WebElement getElementFromHeaderRow(WebElement rowElement, int columnIndex) {
		List<WebElement> listOfElementsInRow = rowElement.findElements(By.tagName("th"));
		int elementCount = listOfElementsInRow.size();
		WebElement result = null;
		for (int i = 0; i < elementCount; i++) {
			if (columnIndex == i) {
				result = listOfElementsInRow.get(i);
				break;
			}
		}
		return result;
	}

	public WebElement getElementFromHeaderRow(WebElement rowElement, String columnName) {
		List<WebElement> listOfElementsInRow = rowElement.findElements(By.tagName("th"));
		int elementCount = listOfElementsInRow.size();
		WebElement result = null;
		for (int i = 0; i < elementCount; i++) {
			WebElement tempElement = listOfElementsInRow.get(i);
			if (tempElement.getText().equals(columnName)) {
				result = tempElement;
				break;
			}
		}
		return result;
	}

	public WebElement getElementFromTable(By bySelector, int rowIndex, int columnIndex, boolean hasHeaderRow) {
		WebElement table = this.getDriver().findElement(bySelector);
		WebElement result = getElementFromTable(table, rowIndex, columnIndex, hasHeaderRow);
		return result;
	}

	public WebElement getElementFromTable(WebElement table, int rowIndex, int columnIndex, boolean hasHeaderRow) {
		WebElement result = null;
		WebElement rowElement = getRowFromTable(table, rowIndex);
		if ((rowIndex > 0) || (!hasHeaderRow)) {
			result = getElementFromDataRow(rowElement, columnIndex);
		} else {
			result = getElementFromHeaderRow(rowElement, columnIndex);
		}
		return result;
	}
	
	public int getNumberOfRowsInTable(By bySelector) {
		try{
			WebElement table = this.getDriver().findElement(bySelector);
			int rowCount = this.getNumberOfRowsInTable(table);
			return rowCount;
		}catch(NoSuchElementException e){
			return 0;
		}
	}
	
	public int getNumberOfDataRowsInTable(By bySelector) {
		try{
			WebElement table = this.getDriver().findElement(bySelector);
			WebElement tableBody = table.findElement(By.tagName("tbody"));
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));		
			int rowCount = rows.size();
			return rowCount;
		}catch(NoSuchElementException e){
			return 0;
		}
	}	

	public int getNumberOfRowsInTable(WebElement table) {
		List<WebElement> rows = getRowsFromTable(table);
		if (!rows.isEmpty()) {
			return rows.size();
		}
		return 0;
	}
	
	public int getNumberOfDataRowsInTable(WebElement table) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));		
			int rowCount = rows.size();
			return rowCount;
		}catch(NoSuchElementException e){
			return 0;
		}
	}	

	public WebElement getRowFromTable(By bySelector, int rowIndex) {
		try{
			WebElement Webtable = this.getDriver().findElement(bySelector);
			List<WebElement> listOfRowElements = Webtable.findElements(By.tagName("tr"));
			int rowCount = listOfRowElements.size();
			WebElement result = null;
			for (int i = 0; i < rowCount; i++) {
				if (rowIndex == i) {
					result = listOfRowElements.get(i);
					break;
				}
			}
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public WebElement getDataRowFromTable(By bySelector, int dataRowIndex) {
		try{
			WebElement table = this.getDriver().findElement(bySelector);
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> dataRows = tableBody.findElements(By.tagName("tr"));
			int dataRowCount = dataRows.size();
			WebElement result = null;
			for (int i = 0; i < dataRowCount; i++) {
				if (dataRowIndex == i) {
					result = dataRows.get(i);
					break;
				}
			}
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}	

	public WebElement getRowFromTable(WebElement table, int rowIndex) {
		try{
			List<WebElement> listOfRowElements = table.findElements(By.tagName("tr"));
			int rowCount = listOfRowElements.size();
			WebElement result = null;
			for (int i = 0; i < rowCount; i++) {
				if (rowIndex == i) {
					result = listOfRowElements.get(i);
					break;
				}
			}
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public WebElement getDataRowFromTable(WebElement table, int dataRowIndex) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> dataRows = tableBody.findElements(By.tagName("tr"));
			int dataRowCount = dataRows.size();
			WebElement result = null;
			for (int i = 0; i < dataRowCount; i++) {
				if (dataRowIndex == i) {
					result = dataRows.get(i);
					break;
				}
			}
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}		

	public List<WebElement> getRowsFromTable(WebElement table) {
		try{
			List<WebElement> rows = table.findElements(By.tagName("tr"));
			return rows;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<WebElement> getTableDataRows(WebElement table) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));
			return rows;
		}catch(NoSuchElementException e){
			return null;
		}
	}	

	/**
	 * Returns list of strings from target web element of the row
	 * 
	 * @param rowElement the web element of the row
	 * @return List of Strings
	 */
	public List<String> getTextFromTableRow(WebElement rowElement) {
		try{
			List<WebElement> myEles = rowElement.findElements(By.tagName("td"));
			List<String> myElesValue = new ArrayList<String>();
			for (WebElement ele : myEles) {
				myElesValue.add(ele.getText());
			}
			return myElesValue;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<String> getTableRowValues(WebElement row) {
		try{
			List<WebElement> cells = row.findElements(By.tagName("td"));
			List<String> values = new ArrayList<String>();
			for (WebElement cell : cells) {
				values.add(cell.getText());
			}
			return values;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public String getTableRowValue(WebElement row, int columnIndex) {
		try{
			List<WebElement> cells = row.findElements(By.tagName("td"));
			String value = cells.get(columnIndex).getText();
			return value;
		}catch(NoSuchElementException e){
			return null;
		}
	}	
	
	public WebElement getTableHeaderRow(WebElement table) {
		try{
			WebElement tableHeader = table.findElement(By.tagName("thead"));
			WebElement headerRow = tableHeader.findElement(By.tagName("tr"));
			return headerRow;
		}catch(NoSuchElementException e){
			return null;
		}
	}		
	
	public List<String> getTableHeaderTexts(WebElement table) {
		try{
			WebElement tableHeader = table.findElement(By.tagName("thead"));
			WebElement headerRow = tableHeader.findElement(By.tagName("tr"));
			List<WebElement> headers = headerRow.findElements(By.tagName("th"));		
			List<String> values = new ArrayList<String>();
			for (WebElement header : headers) {
				values.add(header.getText());
			}
			return values;
		}catch(NoSuchElementException e){
			return null;
		}
	}	
	
	public String getTableHeaderText(WebElement table, int columnIndex) {
		try{
			WebElement tableHeader = table.findElement(By.tagName("thead"));
			WebElement headerRow = tableHeader.findElement(By.tagName("tr"));
			List<WebElement> headers = headerRow.findElements(By.tagName("th"));
			String value = headers.get(columnIndex).getText();
			return value;
		}catch(NoSuchElementException e){
			return null;
		}
	}	
	

	/**
	 * Returns a String of text within a specified WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return String of text within a specified WebElement
	 */
	public String getTextOfElement(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement tempElement = getDriver().findElement(bySelector);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement = wait.until(ExpectedConditions.visibilityOf(tempElement));
		String elementText = myDynamicElement.getText();
		return elementText;
	}

	public String getTextOfElement(By bySelector, int timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds);
		WebElement tempElement = getDriver().findElement(bySelector);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement = wait.until(ExpectedConditions.visibilityOf(tempElement));
		String elementText = myDynamicElement.getText();
		return elementText;
	}

	/**
	 * This method will search the whole web page for web elements using the provided bySelector,
	 * get the text value of each found web elements and add to a list of strings and then returns
	 * the list.
	 * 
	 * @param bySelector the By selector of the element
	 * @return List of string of web elements from target selector
	 */
	public List<String> getTextOfElements(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		List<WebElement> myEles = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(bySelector));
		List<String> myElesText = new ArrayList<String>();
		for (WebElement ele : myEles) {
			myElesText.add(ele.getText());
		}
		return myElesText;
	}

	/**
	 * This method will search the whole web page for web elements using the provided bySelector, 
	 * store the found elements in a list of web elements, and the return the web element in the list
	 * by the index.
	 * 
	 * @param bySelector the By selector of the element
	 * @param index the order of the element in the list
	 * @return the web element in the list
	 */
	public WebElement gettWebElementFromList(By bySelector, int index) {
		List<WebElement> listOfElements = getDriver().findElements(bySelector);
		WebElement element = listOfElements.get(index);
		return element;
	}

	/**
	 * Returns current URL of web browser
	 * 
	 * @return the string of current URL
	 */
	public String getCurrentUrl() {
		String currentUrl = getDriver().getCurrentUrl();
		return currentUrl;
	}

	/**
	 * Returns current string value within an input field
	 * 
	 * @param bySelector
	 *            By Selector of WebElement
	 * @return the string currently inside a text input field
	 */
	public String getValueInput(By bySelector) {
		WebElement inputvalue = getDriver().findElement(bySelector);
		String enterInputvalue = inputvalue.getAttribute("value");
		return enterInputvalue;
	}

	// possible duplicate functionality
	public String getValueOfInput(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		return myDynamicElement.getAttribute("value");
	}
	
	// Set the selected value for a standard select box
	public void setValueSelectBox(By byLocator, String value){
		waitSelectByVisibleText(byLocator, value);
	}	
	
	public void setValueSelectBox(By byLocator, String value, boolean scrollIntoView){
		waitSelectByVisibleText(byLocator, value, scrollIntoView);
	}
	
	// Get the selected value for a standard select box
	public String getValueSelectBox(By byLocator){
		Select select = new Select(getDriver().findElement(byLocator));
		WebElement option = select.getFirstSelectedOption();
		String selectedValue = option.getText();
		return selectedValue;
	}	
	
	

	/**
	 * Returns WebElement defined by the bySelector parameter when it becomes
	 * present
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return the WebElement when it becomes present on the page
	 */
	public WebElement getElementWhenPresent(By bySelector) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		element = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		return element;
	}
	
	public WebElement getElementWhenPresent(By bySelector, long timeout) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
		element = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		return element;
	}	
	

	/**
	 * Returns WebElement defined by the bySelector parameter when it becomes
	 * visible
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return the WebElement which has become visible
	 */
	public WebElement getElementWhenVisible(By bySelector) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(bySelector));
		return element;
	}
	
	public WebElement getElementWhenVisible(By bySelector, long timeout) {
		WebElement element = null;
		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
		element = wait.until(ExpectedConditions.visibilityOfElementLocated(bySelector));
		return element;
	}	

	/**
	 * Navigates browser to a specified URL
	 * 
	 * @param value
	 *            String of URL
	 */
	public void goToUrl(String value) {
		getDriver().get(value);
	}

	/**
	 * Hovers over a given WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void hoverOverElement(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		Actions builder = new Actions(getDriver());
		builder.moveToElement(myDynamicElement).build().perform();
	}
	
	public void moveMouseToElement(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		moveMouseToElement(myDynamicElement);
	}	

	public void moveMouseToElement(WebElement element) {
		Actions builder = new Actions(getDriver());
		Action mouseMove = builder.moveToElement(element).build();
		mouseMove.perform();
	}
	
	/**
	 * This method only work in local, does not work in grid or virtual machine
	 * @param bySelector the web element to work on
	 */
	public void movePhysicalMouseToElement(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		movePhysicalMouseToElement(myDynamicElement);
	}	

	public void movePhysicalMouseToElement(WebElement element) {
		Dimension d = element.getSize();
		Point coordinates = element.getLocation();
		Robot robot;
		try {
			robot = new Robot();
			robot.mouseMove(coordinates.getX() + d.width/2, coordinates.getY() + d.getHeight()/2);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}
			
	/**
	 * Returns boolean value indicating whether or not a specified WebElement is
	 * both displayed and enabled
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return boolean value indicating whether element is both displayed and
	 *         enabled
	 */
	public boolean isDisplayedAndIsEnabled(By bySelector) {
		WebElement myDynamicElement = getDriver().findElement(bySelector);
		boolean isVisible = myDynamicElement.isDisplayed();
		boolean isClickable = myDynamicElement.isEnabled();
		return (isVisible && isClickable);
	}
	
	
	public boolean isDisplayedAndIsEnabled(WebElement element) {
		boolean isVisible = element.isDisplayed();
		boolean isClickable = element.isEnabled();
		return (isVisible && isClickable);
	}	

	/**
	 * Returns boolean indicating whether or not a checkbox is currently
	 * selected
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @return true if the checkbox is selected, false otherwise
	 */
	public boolean isSelectedCheckbox(By bySelector) {
		WebElement myCheckBox = getDriver().findElement(bySelector);
		boolean isSelected = myCheckBox.isSelected();
		return isSelected;
	}

	public boolean isWindow(String value) {
		String currentTitle;
		try {
			currentTitle = getDriver().getTitle();
			if (currentTitle.equalsIgnoreCase(value)) {
				return true;
			}
		} catch (NoSuchWindowException ex) {
			System.out.println("Current window no longer exists");
			return false;
		} catch (Exception ex) {
			return false;
		}
		System.out.println(currentTitle + " != " + value);
		return false;
	}

	/**
	 * Clicks using JavascriptExecutor
	 */
	public void javascriptClick(By bySelector) {
		WebElement myDynamicElement = getDriver().findElement(bySelector);
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("arguments[0].click()", myDynamicElement);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
	
	public void javascriptClick(WebElement myEle) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("arguments[0].click()", myEle);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}

	/**
	 * Clicks using Javascript and accept javascript alert
	 */
	public void javascriptClickAndAcceptAlert(By bySelector) {
		WebElement myDynamicElement = getDriver().findElement(bySelector);
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("arguments[0].click()", myDynamicElement);
			((JavascriptExecutor) getDriver()).executeScript("window.confirm = function(msg) { return true; }");
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}
	
	public void javascriptClickAndAcceptAlert(WebElement myEle) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("arguments[0].click()", myEle);
			((JavascriptExecutor) getDriver()).executeScript("window.confirm = function(msg) { return true; }");
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}

	/**
	 * Sets value using javascript
	 * 
	 */
	public void javascriptSetValue(By bySelector, String value) {
		WebElement targetEle = getDriver().findElement(bySelector);
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("arguments[0].value='" + value + "'", targetEle);
	}
	
	public void javascriptSetValue(WebElement myEle, String value) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("arguments[0].value='" + value + "'", myEle);
	}
	
	public String javascriptGetValue(By bySelector) {
		WebElement myElement = getDriver().findElement(bySelector);		
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		return (String) js.executeScript("return arguments[0].text", myElement);
	}	
	
	public String javascriptGetValue(WebElement myElement) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		return (String) js.executeScript("return arguments[0].text", myElement);
	}	
	
	public String jsGetElementById(String id) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		String tmp = js.executeScript("return document.getElementById('" + id + "').value;").toString();
		return tmp;
	}

	// Unstable
	public void jsSetElementById(String id, String value) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		js.executeScript("document.getElementById('" + id + "').value='" + value + "';");
	}

	/**
	 * Navigates to the previous page, equivalent to pressing the back button on
	 * a browser
	 * 
	 */
	public void navigateToPreviousPage() {
		sleep(5000);
		getDriver().navigate().back();
	}

	/**
	 * Returns the number of rows in the body of a table
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with,
	 *            should be a css element with the tr tag, ex: "tblLogsDta tr"
	 * @return number of rows in the body of a table
	 */
	public int numOfRowsInTableBody(By bySelector){
		List<WebElement> rows = getDriver().findElements(bySelector);
		int numOfRows = rows.size();
		return numOfRows;
	}

	/**
	 * Refreshes page
	 * 
	 */
	public void refresshCurrentWindow() {
		getDriver().navigate().refresh();
	}

	/**
	 * Returns contents of error message so that it can be printed on the
	 * Serenity reports
	 * 
	 * @return string of the error message captured
	 */
	public String returnErrorMessage() {
		getDriver().findElement(toastErrorMessage);
		String errorMessage = getTextOfElement(toastErrorMessage);
		return errorMessage;
	}
 
	/**
	 * Scrolls page to bring the element into viewport
	 */
	public void scrollIntoView(WebElement myElement) {
		//if (!myElement.isDisplayed()) {
			//element is above
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(true);", myElement); 
		//}
		//if (!myElement.isDisplayed()) {
			//element is below
			((JavascriptExecutor) this.getDriver()).executeScript("arguments[0].scrollIntoView(false);", myElement); 
		//}
	}
	
	public void scrollIntoView(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		scrollIntoView(myDynamicElement);
	}
	
	
	public int getViewPortWidth() {
		String value = ((JavascriptExecutor) this.getDriver()).executeScript("return document.documentElement.clientWidth").toString(); 
		int width = Integer.valueOf(value);
		return width;
	}	
			
	public int getViewPortHeight() {
			String value = ((JavascriptExecutor) this.getDriver()).executeScript("return document.documentElement.clientHeight").toString(); 
			int height = Integer.valueOf(value);
			return height;			
	}
	
	public void moveElementToVerticalCenter(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		moveElementToVerticalCenter(myDynamicElement);
	}
	
	public void moveElementToVerticalCenter(WebElement element) {
		int h = getViewPortHeight()/2;
		System.out.println("h =" + h);
		int currentY = element.getLocation().getY();
		System.out.println("currentY =" + currentY);
		boolean moveUp = false;
		if (currentY > h ) {
			moveUp = true;
		}		
		if (moveUp){
			this.scrollPageDown(currentY - h);						
		}else{
				this.scrollPageUp( h - currentY );
		}  
	}
	
	public int getVerticalScrollPosition() {
		String value = ((JavascriptExecutor) this.getDriver()).executeScript("return ((window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop)").toString(); 
		int scrollPosition = Integer.valueOf(value);
		return scrollPosition;
	}

	/**
	 * Scrolls the page x pixels horizontal and x pixels vertical
	 * 
	 * @param pixelsRight
	 *            amount of pixels to scroll horizontally
	 * @param pixelsDown
	 *            amount of pixels to scroll vertically
	 */
	public void scrollPage(String pixelsRight, String pixelsDown) {
		String amountToScroll = "window.scrollTo(" + pixelsRight + "," + pixelsDown + ")";
		JavascriptExecutor js = ((JavascriptExecutor) getDriver());
		js.executeScript(amountToScroll);
	}

	public void scrollPageDown(int pixels) {
		JavascriptExecutor js = ((JavascriptExecutor) getDriver());
		js.executeScript("window.scrollTo(0, " + pixels + ")");
	}

	/**
	 * Scrolls down one page length
	 */
	public void scrollPageLengthDown() {
		JavascriptExecutor js = ((JavascriptExecutor) getDriver());
		js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
	}

	/**
	 * Scrolls 500 pixels to the right
	 */
	public void scrollPageLengthRight() {
		JavascriptExecutor js = ((JavascriptExecutor) getDriver());
		js.executeScript("window.scrollTo(500, 0)");
	}

	public void scrollPageUp(int pixels) {
		JavascriptExecutor js = ((JavascriptExecutor) getDriver());
		js.executeScript("window.scrollTo(0, -" + pixels + ")");
	}

	/**
	 * Select from select drop down input by Index
	 * 
	 */
	public void selectSelectByIndex(By bySelector, int index) {
		WebElement element = new WebDriverWait(getDriver(), defaultTimeout)
				.until(ExpectedConditions.elementToBeClickable(bySelector));
		Select dropdown = new Select(element);
		dropdown.selectByIndex(index);
	}

	/**
	 * Select from select drop down input by value
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param textValue
	 *            String of value to select by
	 */
	public void selectSelectByValue(By bySelector, String textValue) {
		WebElement selector = getDriver().findElement(bySelector);
		ExpectedCondition<WebElement> expectedElement = ExpectedConditions.elementToBeClickable(selector);
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(expectedElement);
		Select dropdown = new Select(myDynamicElement);
		dropdown.selectByValue(textValue);
	}

	/**
	 * Select from select drop down input by visible text when element is clickable
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param textValue
	 *            String of text value to select by
	 */
	public void selectSelectByVisibleText(By bySelector, String textValue) {
		WebElement element = new WebDriverWait(getDriver(), defaultTimeout)
				.until(ExpectedConditions.elementToBeClickable(bySelector));
		Select dropdown = new Select(element);
		dropdown.selectByVisibleText(textValue);
	}

	/**
	 * Select from Angular UI select drop down by visible text
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param textValue
	 *            String of text value to select by
	 */
	public void selectUISelectByVisibleText(By bySelector, String textValue){
		waitClick(By.cssSelector("a.select2-choice.ui-select-match.select2-default"));
		sleep(5000);
		Actions builder = new Actions(getDriver());
		builder.sendKeys(textValue).build().perform();
		sleep(5000);
		builder.sendKeys(Keys.ENTER).build().perform();
	}

	public void sendDeleteKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.DELETE);
	}

	/**
	 * Send Home key to browser
	 */
	public void sendEndKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.END).build().perform();
	}

	/**
	 * Sends Enter key to to WebElement specified
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void sendEnterKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.ENTER);
	}

	/**
	 * Sends Enter key to browser
	 */
	public void sendEnterKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.ENTER).build().perform();
	}

	/**
	 * Sends Esc key to WebElement specified
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void sendEscKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.ESCAPE);
	}

	/**
	 * Sends Esc key to browser
	 */
	public void sendEscKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.ESCAPE).build().perform();
	}

	/**
	 * Sends Home key to browser
	 */
	public void sendHomeKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.HOME).build().perform();
	}
	
    /**
     * Sends key to page
     * 
     * @param key the key name
     */
	public void sendKeysToPage(String key) {
		Actions action = new Actions(getDriver());
		action.sendKeys(key).build().perform();
	}

	/**
	 * Sends Return key to to WebElement specified
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void sendReturnKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.RETURN);
	}

	/**
	 * Sends Return key to browser
	 */
	public void sendReturnKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.RETURN).build().perform();
	}

	/**
	 * Sends Space key to to WebElement specified
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void sendSpaceKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.SPACE);
	}

	/**
	 * Sends Space key to browser
	 */
	public void sendSpaceKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.SPACE).build().perform();
	}

	/**
	 * Sends Tab key to to WebElement specified
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void sendTabKey(By bySelector) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		myDynamicElement.sendKeys(Keys.TAB);
	}

	/**
	 * Sends Tab key to browser
	 */
	public void sendTabKeyToPage() {
		Actions builder = new Actions(getDriver());
		builder.sendKeys(Keys.TAB).perform();
	}

	/**
	 * Waits for a checkbox to be present on the page and sets the value of that
	 * checkbox
	 * 
	 * @param bySelector
	 *            By Selector of the checkbox that is being interacted with
	 * @param value
	 *            boolean value
	 */

	public void setCheckBox(By bySelector, boolean value) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		this.setCheckbox(myDynamicElement, value);
	}
	
	public void setCheckBox(By bySelector, boolean value, boolean scrollIntoView) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		if (scrollIntoView){
			scrollIntoView(myDynamicElement);
		}
		this.setCheckbox(myDynamicElement, value);
	}	

	/**
	 * Similar to public void setValue(By bySelector, String value) but not
	 * clear element. It waits for a WebElement to be present on the page and sets
	 * the value of that WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param value
	 *            String value which is set into the WebElement
	 */
	public void setValueOnly(By bySelector, String value) {
		setValueOnly(bySelector, value, false);
	}
	
	public void setValueOnly(By bySelector, String value, boolean scrollIntoView) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		if (scrollIntoView){
			scrollIntoView(myDynamicElement);
		}
		myDynamicElement.sendKeys(value);
	}
	
	/**
	 *  This method will use multiple method to set value to a text field. 
	 * 
	 */	
	public void setValueEx(By bySelector, String value) {
		setValueEx(bySelector, value, true);
	}
	
	public void setValueEx(By bySelector, String value, boolean scrollIntoView) {
		WebElement myDynamicElement = waitUntilAppear(bySelector);
		myDynamicElement = waitUntilClickable(bySelector);
		if (scrollIntoView){
			scrollIntoView(myDynamicElement);
		}
		// use WebElement method
		setValue(bySelector, value);
		String result = myDynamicElement.getText();
	    if (result.equals(value)){
			return;
		}
	    // use attribute:
		setAttributeValue(myDynamicElement, "value", value);
		result = getAttributeValue(myDynamicElement, "value");					
	    if (result.equals(value)){
			return;
		}
	    // use javasript as the last resort:
	    javascriptSetValue(myDynamicElement, value);	    	    
	}	
		

	/**
	 * Waits for a WebElement to be present on the page and sets the value of
	 * that WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param value
	 *            integer value which is set into the WebElement
	 */
	public void setValue(By bySelector, int value) {
		String valueAsString = Integer.toString(value);
		setValue(bySelector, valueAsString);
	}
	
	public void setValue(By bySelector, int value, boolean scrollIntoView) {
		String valueAsString = Integer.toString(value);
		setValue(bySelector, valueAsString, scrollIntoView);
	}	

	/**
	 * Waits for a WebElement to be present on the page and sets the value of
	 * that WebElement
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param value
	 *            String value which is set into the WebElement
	 */
	public void setValue(By bySelector, String value) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		clearElement(bySelector);
		myDynamicElement.sendKeys(value);
	}
	
	public void setValue(By bySelector, String value, boolean scrollIntoView) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		if (scrollIntoView){
			scrollIntoView(myDynamicElement);
		}
		clearElement(bySelector);
		myDynamicElement.sendKeys(value);
	}	

	/**
	 * Changes focus of WebDriver to a pop up or second window opened by the
	 * original browser window
	 */
	public void switchToChildWindow() {
		Set<String> winHandleBefore = getDriver().getWindowHandles();
		int length = winHandleBefore.size() - 1;
		getDriver().switchTo().window(winHandleBefore.toArray()[length].toString());
	}

	/**
	 * Changes focus of WebDriver to the original window of browser
	 */
	public void switchToParentWindow() {
		Set<String> winHandleBefore1 = getDriver().getWindowHandles();
		getDriver().switchTo().window(winHandleBefore1.toArray()[0].toString());
	}

	public boolean switchToWindow(String value) {
		String currentWinHandle = null;
		String currentTitle = "";
		try {
			currentWinHandle = getDriver().getWindowHandle();
			currentTitle = getDriver().getTitle();
			System.out.println("Current window: " + currentTitle);
		} catch (Exception ex) {
			currentWinHandle = null;
		}
		Set<String> winHandles = getDriver().getWindowHandles();
		try {
			for (String winHandle : winHandles) {
				System.out.println(winHandle);
				getDriver().switchTo().window(winHandle);
				String title = getDriver().getTitle();
				System.out.println("Switched to " + title);
				if (value.equalsIgnoreCase(title)) {
					return true;
				}
			}
		} catch (Exception ex) {
			// Not found new window, switch back to current window
			if (currentWinHandle != null) {
				getDriver().switchTo().window(currentWinHandle);
				System.out.println("Switched back to " + currentTitle);
			}
			return false;
		}
		if (currentWinHandle != null) {
			getDriver().switchTo().window(currentWinHandle);
			System.out.println("Switched back to " + currentTitle);
		}
		return false;
	}

	/**
	 * Takes screenshot
	 * 
	 * @param bySelector
	 * @param filePath
	 * @throws IOException
	 */
	public void takeScreenshotOfWebElement(By bySelector, String filePath) throws IOException {
		WebElement ele = getDriver().findElement(bySelector);
		// Get entire page screenshot
		File screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
		BufferedImage fullImg = ImageIO.read(screenshot);
		// Get width and height of entire screen
		int height = fullImg.getHeight();
		int width = fullImg.getWidth();
		System.out.println(height);
		System.out.println(width);
		// Get the location of element on the page
		Point point = ele.getLocation();
		// Get width and height of the element
		int eleWidth = ele.getSize().getWidth();
		int eleHeight = ele.getSize().getHeight();
		System.out.println(eleWidth);
		System.out.println(eleHeight);
		// Account for whether height or width of element is greater than image
		if ((point.getY() + eleHeight) > height) {
			int differenceInHeight = height - point.getY();
			eleHeight = differenceInHeight;
			System.out.println("point.getY() + eleHeight) > height");
		}
		if ((point.getX() + eleWidth) > width) {
			int differenceInWidth = width - point.getX();
			eleWidth = differenceInWidth;
			System.out.println("point.getX() + eleWidth) > width");
		}
		// Crop the entire page screenshot to get only element screenshot
		BufferedImage eleScreenshot = fullImg.getSubimage(point.getX(), point.getY(), eleWidth, eleHeight);
		ImageIO.write(eleScreenshot, "png", screenshot);
		File screenshotLocation = new File(filePath);
		FileUtils.copyFile(screenshot, screenshotLocation);
	}

	/**
	 * Uploads a file to the selector
	 * 
	 * @param selector
	 *            By Selector of WebElement that is being interacted with
	 * @param Path
	 *            String of file path to file to be uploaded
	 */
	public void uploadFile(By selector, String Path){
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement upload = wait.until(ExpectedConditions.presenceOfElementLocated(selector));
		upload.findElement(selector);
		upload.sendKeys(Path);
	}

	/**
	 * Waits for WebElement defined by the bySelector parameter to be clickable
	 * before attempting to click on it
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 */
	public void waitClick(By bySelector) {
		waitClick(bySelector, defaultTimeout);
	}
	
	public void waitClick(By bySelector, boolean scrollIntoView) {
		waitClick(bySelector, defaultTimeout, scrollIntoView);
	}	

	/**
	 * Waits for WebElement defined by the bySelector parameter to be clickable
	 * before attempting to click on it
	 * 
	 * @param bySelector
	 *            By Selector of WebElement that is being interacted with
	 * @param timeout
	 *            Time which WebDriver should wait
	 */
	public void waitClick(By bySelector, long timeout) {
		WebElement myDynamicElement = new WebDriverWait(getDriver(), timeout)
				.until(ExpectedConditions.elementToBeClickable(bySelector));
		myDynamicElement.click();
	}
	
	public void waitClick(By bySelector, long timeout, boolean scrollIntoView) {
		WebElement myDynamicElement = new WebDriverWait(getDriver(), timeout)
				.until(ExpectedConditions.elementToBeClickable(bySelector));
		if (scrollIntoView){
			scrollIntoView(myDynamicElement);
		}
		myDynamicElement.click();
	}
		
	/**
	 *  This method applies extreme measures to try to click/select a radio button 
	 * 
	 */
	public void clickRadioButtonEx(By bySelector){
		clickRadioButtonEx(bySelector, 10, true);
	}	
	
	public void clickRadioButtonEx(By bySelector, int maxTries, boolean scrollIntoView){	
		WebElement element = this.waitUntilClickable(bySelector);
		if (scrollIntoView){
			scrollIntoView(element);
		}		
		boolean selected = element.isSelected();
		int n = 0;
		while (!selected && n < maxTries ){
			element.click();
			this.clickOn(element);
			this.javascriptClick(element); 
			waitClick(bySelector);
			selected = element.isSelected();
			n++;
			sleep(500);
		}	
	}	
	

	/**
	 * Waits for the loading animation to disappear
	 * 
	 */
	public void waitForLoading() {
		waitForLoading(defaultTimeout);
	}

	/**
	 * Waits for the loading animation to disappear
	 * 
	 * @param timeoutSeconds seconds to specify the timeout
	 */
	public void waitForLoading(long timeoutInSeconds){			
		waitUntilFinish(busyloadAnimation, timeoutInSeconds);
	}
	
	private void waitForP2Loading() {
		if (isP2Cucumber) {
			List<WebElement> webElements = getDriver().findElements(busyloadAnimation);
			if (webElements.size()>0) {
				WebDriverWait wait = new WebDriverWait(getDriver(), this.defaultTimeout );
				wait.until(ExpectedConditions.invisibilityOfAllElements(webElements));
			}
		}
	}

	/**
	 * Waits for WebElement defined by the bySelector parameter to be visible
	 * 
	 * @param bySelector the By Selector of WebElement that is being interacted with
	 * @param timeout amount of time in milliseconds to wait
	 */
	public WebElement waitUntilAppear(By bySelector, long timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
		WebElement myDynamicElement =  wait.until(ExpectedConditions.visibilityOfElementLocated(bySelector));
		return myDynamicElement;
	}
	
	public void waitUntilDisappear(By selector, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds );
		wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
	}

	public WebElement waitUntilClickable(By bySelector) {
		return waitUntilClickable(bySelector, defaultTimeout);
	}
	
	public WebElement waitUntilClickable(By bySelector, long timeout) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeout);
		WebElement myDynamicElement =  wait.until(ExpectedConditions.elementToBeClickable(bySelector));
		return myDynamicElement;
	}	

	/**
	 * Selects from select drop down input by visible text when element is present
	 * 
	 * @param bySelector the By selector of web element that is being interacted with
	 * @param value text value to select by
	 */
	public void waitSelectByVisibleText(By bySelector, String value) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		Select select = new Select(myDynamicElement);
		select.selectByVisibleText(value);
	}
	
	public void waitSelectByVisibleText(By bySelector, String value, boolean scrollIntoView) {
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.presenceOfElementLocated(bySelector));
		if(scrollIntoView){
			scrollIntoView(bySelector);
		}
		Select select = new Select(myDynamicElement);
		select.selectByVisibleText(value);
	}	

	/**
	 * Wait until the selector appears
	 * 
	 * @param selector By selector of WebElement that is being interacted with
	 */
	public WebElement waitUntilAppear(By selector){
		return waitUntilAppear(selector, defaultTimeout);
	}

	/**
	 * Wait until the selector is finished and disappears
	 * 
	 */
	public void waitUntilFinish(By selector) {
		waitUntilFinish(selector, defaultTimeout);
	}


	public void waitUntilFinish(By selector, long timeoutSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutSeconds);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
	}
	
	/**
	 * Gets color of an element
	 * 
	 * @param element the WebElement
	 * @param colorProperty such as "color", "background-color", "border-color", 
	 * 						"border-top/bottom/left/right-color"
	 * @return a string of RGBa such as "rgba(0, 255, 0, 1)"
	 */
	public String getElementColor(WebElement element, String colorProperty){
		return element.getCssValue(colorProperty);
	}
	
	public String getElementBackgroundColor(WebElement element){
		return element.getCssValue("background-color");
	}
	
	public String getElementColor(WebElement element){
		return element.getCssValue("color");
	}
	
	public String getElementBorderColor(WebElement element){
		return element.getCssValue("border-color");
	}	
	
	/**
	 * Get color of an element
	 * 
	 * @param bySelector
	 * @param colorProperty such as "color", "background-color", "border-color", 
	 * 						"border-top/bottom/left/right-color"
	 * 						
	 * @return a string of RGBa such as "rgba(0, 255, 0, 1)"
	 */
	public String getElementColor(By bySelector, String colorProperty){
		WebDriverWait wait = new WebDriverWait(getDriver(), defaultTimeout);
		WebElement myDynamicElement = wait.until(ExpectedConditions.visibilityOfElementLocated(bySelector));
		return myDynamicElement.getCssValue(colorProperty);
	}
	
	public String getElementBorderColor(By bySelector){		
		return getElementColor(bySelector, "border-color");
	}	
	
	public String getElementBackgroundColor(By bySelector){		
		return getElementColor(bySelector, "background-color");
	}
	
	public String getElementTextColor(By bySelector){		
		return getElementColor(bySelector, "color");
	}	
	
    /**
     * Get the tooltip when hovering on the web element
     * 
     */
	public String getToolTipOnElement(By byElementSelector, By byToopTipSelector) throws InterruptedException{
		String toolTipText = "";
		WebElement webElement = this.getDriver().findElement(byElementSelector);
		webElement.click();
		Thread.sleep(1000);
		// Use Action class to hover the mouse over the web element
		Actions action = new Actions(this.getDriver());
		action.moveToElement(webElement).build().perform();
		// Find the tool tip element and get the tool tip text
		try{
			WebElement toolTipElement = this.getDriver().findElement(byToopTipSelector); 			
			toolTipText = toolTipElement.getText();
		}catch(Exception ex){
			toolTipText = "";
		}
		return toolTipText;
	}	
		
	public void sleep(long miliiSeconds){
		try{
			Thread.sleep(miliiSeconds);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}
}
