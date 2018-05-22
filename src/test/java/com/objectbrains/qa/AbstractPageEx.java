package com.objectbrains.qa;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileReader;
import java.util.Properties;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.Augmenter;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import net.serenitybdd.core.pages.PageObject;

public class AbstractPageEx extends PageObject{
	public static final String FILE_EXTENSION_PNG = ".png";
	public static final String FILE_FORMAT_PNG = "png";
	public static final String JENKINS_CONFIG_FILENAME = "jenkins.config.properties";
	public static final String KEY_GLOBAL_TIMEOUT = "test.globaltimeout";
	public static final String KEY_PROJECT_NAME = "test.projectname";
	public static final long MAX_GLOBAL_TIMEOUT_IN_SECONDS = 60;
	public static final long GLOBAL_TIMEOUT = loadGlobalTimeout(KEY_GLOBAL_TIMEOUT, JENKINS_CONFIG_FILENAME, MAX_GLOBAL_TIMEOUT_IN_SECONDS);
	
	private final String PROJECT_NAME = readValueFromPropertiesFile(KEY_PROJECT_NAME, JENKINS_CONFIG_FILENAME);
	private final boolean isP2Cucumber = PROJECT_NAME.equalsIgnoreCase("P2");
	private final boolean isP1Cucumber = PROJECT_NAME.equalsIgnoreCase("P1");


	public AbstractPageEx() {
		super();
	}
	
	private static long loadGlobalTimeout(String key, String filename, long maxValue) {
		long timeout = 0;
		try {
			String value = readValueFromPropertiesFile(key, filename);
			timeout = Long.parseLong(value);
			if (timeout > maxValue) {
				timeout = maxValue;
			}
		} catch (NumberFormatException ex) {
			ex.printStackTrace();
		}
		return timeout;
	}

	public static String readValueFromPropertiesFile(String key, String filename) {
		String value = "";
		Properties props = null;
		File file = new File(filename);
		try {
			FileReader reader = new FileReader(file);
			props = new Properties();
			props.load(reader);
			String result = props.getProperty(key);
			reader.close();
			if (result != null && !result.isEmpty()) {
				value = result.trim();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		}
		return value;
	}

	// 1). Alert MessageBox Methods =====================//
	public String alertMessageGetText() {
		Alert alert = getDriver().switchTo().alert();
		return alert.getText();
	}

	public String alertMessageGetTextWait(long timeoutInSeconds) {
		waitUntilAlertPresent(timeoutInSeconds);
		Alert alert = getDriver().switchTo().alert();
		return alert.getText();
	}

	public void alertMessageSendKeys(String keys) {
		Alert alert = getDriver().switchTo().alert();
		alert.sendKeys(keys);
	}

	public void alertMessageSendKeysWait(String keys, long timeoutInSeconds) {
		waitUntilAlertPresent(timeoutInSeconds);
		Alert alert = getDriver().switchTo().alert();
		alert.sendKeys(keys);
	}

	public void alertMessageAccept() {
		Alert alert = getDriver().switchTo().alert();
		alert.accept();
	}

	public void alertMessageAcceptWait(long timeoutInSeconds) {
		waitUntilAlertPresent(timeoutInSeconds);
		Alert alert = getDriver().switchTo().alert();
		alert.accept();
	}

	public void alertMessageDismiss() {
		Alert alert = getDriver().switchTo().alert();
		alert.dismiss();
	}

	public void alertMessageDismissWait(long timeoutInSeconds) {
		waitUntilAlertPresent(timeoutInSeconds);
		Alert alert = getDriver().switchTo().alert();
		alert.dismiss();
	}

	// 2). Click Methods Using Actions Class
	/**
	 * Waits until this web element becomes clickable then clicks it.
	 */
	public void clickActionWait(By byLocator, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Actions actions = new Actions(getDriver());
		actions.click(webElement).build().perform();
	}

	public void clickActionWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		Actions actions = new Actions(getDriver());
		actions.click(webElement).build().perform();
	}

	public void clickAction(By byLocator, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Actions actions = new Actions(getDriver());
		actions.click(webElement).build().perform();
	}

	public void clickAction(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		Actions actions = new Actions(getDriver());
		actions.click(webElement).perform();
	}
	
	public void clickDoubleAction(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		Actions actions = new Actions(getDriver());
		actions.doubleClick(webElement).perform();
	}
	
	public void clickDoubleAction(By byLocator, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Actions actions = new Actions(getDriver());
		actions.doubleClick(webElement).perform();
	}
	
	public void clickDoubleAndSetValueAction(By byLocator, String value) {
		WebElement webElement = getDriver().findElement(byLocator);
		Actions actions = new Actions(getDriver());
		actions.doubleClick(webElement).sendKeys(value).build().perform();
	}
	
	public void clickDoubleAndSetValueAction(By byLocator, String value, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Actions actions = new Actions(getDriver());
		actions.doubleClick(webElement).moveToElement(webElement).sendKeys(value).build().perform();
	}

	// 3). Capture Screenshot Methods
	/**
	 * Captures screenshot of page and save as a png file using the provided
	 * filename (no extension) at the provided directory. Use FILE_EXTENSION_PNG to
	 * store capture image for automation test.
	 * 
	 * @param directory
	 *            The relative path to directory screenshots should be stored in.
	 * @param fileName
	 *            The name of the file (without extension) to store the captured
	 *            screenshot.
	 * @param fileExtension
	 *            The file extension to store the captured screenshot, should be
	 *            ".png"
	 * @return Returns the file path where file is located.
	 */
	public String captureScreenshot(String directory, String fileName, String fileExtention) throws IOException {
		WebDriver augmentedDriver = new Augmenter().augment(getDriver());
		File screenshotFile = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
		File targetFile = new File(directory, fileName + fileExtention);
		FileUtils.copyFile(screenshotFile, targetFile);
		return targetFile.getAbsolutePath();
	}

	/**
	 * Takes screenshot of a single web element Use FILE_FORMAT_PNG to store as
	 * "pgn" file format for automation test.
	 */
	public void captureElementScreenshot(By byLocator, String filePath, String fileFormat) throws IOException {
		WebElement webElement = getDriver().findElement(byLocator);
		// Get entire page screenshot
		File screenshot = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
		BufferedImage fullImg = ImageIO.read(screenshot);
		int screenHeight = fullImg.getHeight();
		int screenWidth = fullImg.getWidth();
		Point webElementLocation = webElement.getLocation();
		int elementWidth = webElement.getSize().getWidth();
		int elementHeight = webElement.getSize().getHeight();
		// Account for whether height or width of element is greater than image
		if ((webElementLocation.getY() + elementHeight) > screenHeight) {
			int differenceInHeight = screenHeight - webElementLocation.getY();
			elementHeight = differenceInHeight;
		}
		if ((webElementLocation.getX() + elementWidth) > screenWidth) {
			int differenceInWidth = screenWidth - webElementLocation.getX();
			elementWidth = differenceInWidth;
		}
		// Crop the entire page screenshot to get only element screenshot
		BufferedImage elementScreenshot = fullImg.getSubimage(webElementLocation.getX(), webElementLocation.getY(),
				elementWidth, elementHeight);
		ImageIO.write(elementScreenshot, fileFormat, screenshot);
		File screenshotLocation = new File(filePath);
		FileUtils.copyFile(screenshot, screenshotLocation);
	}

	// 4). Is Element/Window Present/Displayed/Enabled/Visible/Clickable dethods
	/**
	 * Because we check its current status so we do not need to wait or scroll into
	 * viewport for all below methods
	 */

	/**
	 * Present means it is on the DOM
	 */
	public boolean isElementPresent(By byLocator) {
		try {
			getDriver().findElement(byLocator);
			return true;
		} catch (NoSuchElementException e) {
			return false;
		}
	}

	/**
	 * Displayed means it is present and not hidden
	 */
	public boolean isElementDisplayed(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		boolean isDisplayed = webElement.isDisplayed();
		return isDisplayed;
	}

	public boolean isElementDisplayed(WebElement webElement) {
		boolean isDisplayed = webElement.isDisplayed();
		return isDisplayed;
	}

	/**
	 * Visual means it is displayed and its dimensions are greater than zero.
	 */
	public boolean isElementVisual(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		boolean isElementVisual = isElementVisual(webElement);
		return isElementVisual;
	}

	public boolean isElementVisual(WebElement webElement) {
		boolean isNotZeroSize = (webElement.getSize().width > 0) && (webElement.getSize().height > 0);
		boolean isDisplayed = webElement.isDisplayed();
		boolean isElementVisual = isNotZeroSize && isDisplayed;
		return isElementVisual;
	}

	/**
	 * Enabled means it can be interacted with.
	 */
	public boolean isElementEnabled(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		boolean isEnabled = webElement.isEnabled();
		return isEnabled;
	}

	/**
	 * Element is clickable means it is visual, and enabled.
	 */
	public boolean isElementClickable(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		boolean isClickable = isElementClickable(webElement);
		return isClickable;
	}

	public boolean isElementClickable(WebElement webElement) {
		boolean isEnabled = webElement.isEnabled();
		boolean isDisplayed = webElement.isDisplayed();
		boolean isNotZeroSize = (webElement.getSize().width > 0) && (webElement.getSize().height > 0);
		boolean isClickable = isNotZeroSize && isDisplayed && isEnabled;
		return isClickable;
	}

	/**
	 * Checks if this web element is present and selected.
	 */
	public boolean isElementSelected(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		boolean isSelected = webElement.isSelected();
		return isSelected;
	}

	/**
	 * Checks if this window with given title exists
	 */
	public boolean isWindowPresent(String title) {
		String current = getDriver().getTitle();
		if (switchToWindow(title)) {
			switchToWindow(current);
			return true;
		}
		return false;
	}

	public boolean isCurrentWindow(String title) {
		String currentTitle = getDriver().getTitle();
		if (currentTitle.equalsIgnoreCase(title)) {
			return true;
		}
		return false;
	}

	// 5). Clear TextBox Content Methods
	/**
	 * Waits for a web element to be present on the page and becomes clickabe then
	 * clears the contents of that web element.
	 */
	public void clearTextBoxWait(By byLocator, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		webElement.clear();
	}

	public void clearTextBoxWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		webElement.clear();
	}

	/**
	 * Clears the content of this TextBox (no wait).
	 */

	public void clearTextBox(By byLocator, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		clearTextBox(webElement);
	}

	public void clearTextBox(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		clearTextBox(webElement);
	}

	public void clearTextBox(WebElement webElement) {
		webElement.clear();
	}

	// 6). Close or Maximize Window Methods
	public void closeCurrentWindow() {
		getDriver().close();
	}

	public void closeWindow(String title) {
		if (switchToWindow(title)) {
			this.closeCurrentWindow();
		}
	}

	public void maximizeWindow() {
		getDriver().manage().window().maximize();
	}

	public void setWindowPosition(Point targetPosition) {
		getDriver().manage().window().setPosition(targetPosition);
		;
	}

	// 7). Driver and Browser Methods
	public void deleteAllCookies() {
		getDriver().manage().deleteAllCookies();
	}

	public void quitDriver() {
		getDriver().quit();
	}

	public void closeDriver() {
		getDriver().close();
	}

	// 8). Get and Set Attribute Methods
	/**
	 * Gets attribute value of this web element. No need to scroll into viewport.
	 */
	public String getAttribute(WebElement webElement, String attributeName) {
		String attributeValue = webElement.getAttribute(attributeName);
		return attributeValue;
	}

	public String getAttribute(By byLocator, String attributeName) {
		WebElement webElement = getDriver().findElement(byLocator);
		return getAttribute(webElement, attributeName);
	}

	public String getAttributeWait(By byLocator, String attributeName, long timoutInSeconds) {
		WebElement webElement = waitUntilPresent(byLocator, timoutInSeconds);
		return getAttribute(webElement, attributeName);
	}

	/**
	 * There is no set attribute method in Selenium. So we have to use JavaScript to
	 * do it. No need to scroll this web element into viewport.
	 */
	public void setAttributeJavaScript(WebElement webElement, String attributeName, String attributeValue) {
		((JavascriptExecutor) getDriver()).executeScript(
				"arguments[0].setAttribute('" + attributeName + "',arguments[1]);", webElement, attributeValue);
	}

	public void setAttributeJavaScript(By byLocator, String attributeName, String attributeValue) {
		WebElement webElement = getDriver().findElement(byLocator);
		setAttributeJavaScript(webElement, attributeName, attributeValue);
	}

	public void setAttributeJavaScriptWait(By byLocator, String attributeName, String attributeValue,
			long timeoutInSeconds) {
		WebElement webElement = waitUntilPresent(byLocator, timeoutInSeconds);
		setAttributeJavaScript(webElement, attributeName, attributeValue);
	}

	// 9). CheckBox Methods
	/**
	 * Waits for a checkbox to be present on the page and becomes clickable then
	 * sets the value of this checkbox
	 */
	public void setCheckBox(WebElement webElement, boolean value, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		boolean isChecked = webElement.isSelected();
		if (value != isChecked) {
			webElement.click();
		}
	}

	public void setCheckBox(WebElement webElement, boolean value) {
		boolean isChecked = webElement.isSelected();
		if (value != isChecked) {
			webElement.click();
		}
	}

	public void setCheckBox(By byLocator, boolean value, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		setCheckBox(webElement, value, scrollIntoViewport);
	}

	public void setCheckBox(By byLocator, boolean value) {
		setCheckBox(byLocator, value);
	}

	public void setCheckBoxWait(By byLocator, boolean value, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		setCheckBox(webElement, value, scrollIntoViewport);
	}

	public void setCheckBoxWait(By byLocator, boolean value, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		setCheckBox(webElement, value);
	}

	public boolean isCheckBoxSelected(WebElement webElement) {
		return webElement.isSelected();
	}

	public boolean isCheckBoxSelected(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		return webElement.isSelected();
	}

	public boolean isCheckBoxSelectedWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(byLocator, timeoutInSeconds);
		return isCheckBoxSelected(webElement);
	}

	// 10). Get Text of Web Element Methods
	/**
	 * Get text of this web element. No need to scroll into Viewport
	 * 
	 */
	public String getText(WebElement webElement) {
		String elementText = webElement.getText();
		return elementText;
	}

	public String getText(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		return getText(webElement);
	}

	public String getTextWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(byLocator, timeoutInSeconds);
		return getText(webElement);
	}

	/**
	 * Uses JavaScript to get text in case other methods fail.
	 */
	public String getTextJavaScript(WebElement myElement) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		return (String) js.executeScript("return arguments[0].text", myElement);
	}

	public String getTextJavaScript(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		return getTextJavaScript(webElement);
	}

	// 11). Get Web Elements Methods
	/**
	 * Searches the whole web page for web elements by the provided byLocator and
	 * returns a list of found web elements.
	 */
	public List<WebElement> getElements(By byLocator) {
		List<WebElement> webElements = getDriver().findElements(byLocator);
		return webElements;
	}

	public List<WebElement> getElementsWait(By byLocator, long timeoutInSeconds) {
		List<WebElement> webElements = waitUntilAllPresent(byLocator, timeoutInSeconds);
		return webElements;
	}

	// 12). Get Value of Web Element Methods
	/**
	 * Gets the value of the attribute "value" of the web element. The wait version
	 * will wait until the web element is present ore timeout expires. There is no
	 * need to scroll the element into viewport.
	 */
	public String getValue(WebElement webElement) {
		String value = webElement.getAttribute("value");
		return value;
	}

	public String getValue(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		return getValue(webElement);
	}

	public String getValueWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilPresent(byLocator, timeoutInSeconds);
		return webElement.getAttribute("value");
	}

	// 13). Move Mouse Methods (also called hover mouse)
	/**
	 * Moves the mouse to a web element also called "hovers the mouse over a web
	 * element" Assuming that the web element already in the viewport because we
	 * need to know where the element is on the page.
	 * 
	 */
	public void moveMouseToElement(WebElement webElement) {
		Actions actions = new Actions(getDriver());
		actions.moveToElement(webElement).perform();
	}

	public void moveMouseToElement(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		moveMouseToElement(webElement);
	}

	public void moveMouseToElementWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(byLocator, timeoutInSeconds);
		moveMouseToElement(webElement);
	}

	/**
	 * Moves the physical mouse pointer to the center of this web element. Moves
	 * physical mouse pointer methods only work on local machine, it does not work
	 * on selenium grid or on virtual machine
	 */
	public void movePhysicalMouse(int x, int y) {
		Robot robot;
		try {
			robot = new Robot();
			robot.mouseMove(x, y);
		} catch (AWTException e) {
			e.printStackTrace();
		}
	}

	public void movePhysicalMouseToElement(WebElement webElement) {
		Dimension elementSize = webElement.getSize();
		Point elementLocation = webElement.getLocation();
		int elementCenterX = elementLocation.getX() + elementSize.width / 2;
		int elementCenterY = elementLocation.getY() + elementSize.getHeight() / 2;
		movePhysicalMouse(elementCenterX, elementCenterY);
	}

	public void movePhysicalMouseToElementWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(byLocator, timeoutInSeconds);
		movePhysicalMouseToElement(webElement);
	}

	public void movePhysicalMouseToElement(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		movePhysicalMouseToElement(webElement);
	}

	// 14). Scroll Page Methods
	/**
	 * Scrolls the page x pixels horizontal and x pixels vertical
	 * 
	 * @param pixelsRight
	 *            The Amount of pixels to scroll horizontally
	 * @param pixelsDown
	 *            The amount of pixels to scroll vertically
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

	// 15). Scroll Element and Viewport Methods
	/**
	 * Scrolls page to bring the element into viewport
	 */
	public void scrollElementIntoViewport(WebElement myElement) {
		try {
			// element is above
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(true);", myElement);
			// element is below
			((JavascriptExecutor) getDriver()).executeScript("arguments[0].scrollIntoView(false);", myElement);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void scrollElementIntoViewport(By byLocator) {
		WebElement webElement = this.getDriver().findElement(byLocator);
		scrollElementIntoViewport(webElement);
	}

	public void scrollElementIntoViewportWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilPresent(byLocator, timeoutInSeconds);
		scrollElementIntoViewport(webElement);
	}

	public void scrollElementToVerticalCenter(WebElement element) {
		int halfViewportHeight = getViewportHeight() / 2;
		int currentY = element.getLocation().getY();
		boolean moveUp = false;
		if (currentY > halfViewportHeight) {
			moveUp = true;
		}
		if (moveUp) {
			this.scrollPageDown(currentY - halfViewportHeight);
		} else {
			this.scrollPageUp(halfViewportHeight - currentY);
		}
	}

	public void scrollElementToVerticalCenterWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(byLocator, timeoutInSeconds);
		scrollElementToVerticalCenter(webElement);
	}

	public int getViewportWidth() {
		String value = ((JavascriptExecutor) this.getDriver())
				.executeScript("return document.documentElement.clientWidth").toString();
		int width = Integer.valueOf(value);
		return width;
	}

	public int getViewportHeight() {
		String value = ((JavascriptExecutor) this.getDriver())
				.executeScript("return document.documentElement.clientHeight").toString();
		int height = Integer.valueOf(value);
		return height;
	}

	public int getVerticalScrollPosition() {
		String value = ((JavascriptExecutor) this.getDriver()).executeScript(
				"return ((window.pageYOffset !== undefined) ? window.pageYOffset : (document.documentElement || document.body.parentNode || document.body).scrollTop)")
				.toString();
		int scrollPosition = Integer.valueOf(value);
		return scrollPosition;
	}

	// 16). SelectBox Methods
	public void setSelectBoxByIndex(WebElement webElement, int index, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Select dropdownList = new Select(webElement);
		dropdownList.selectByIndex(index);
	}

	public void setSelectBoxByIndex(WebElement webElement, int index) {
		Select dropdownList = new Select(webElement);
		dropdownList.selectByIndex(index);
	}

	public void setSelectBoxByIndexWait(By byLocator, int index, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Select dropdownList = new Select(webElement);
		dropdownList.selectByIndex(index);
	}

	public void setSelectBoxByIndexWait(By byLocator, int index, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		Select dropdownList = new Select(webElement);
		dropdownList.selectByIndex(index);
	}
	
	public void setSelectBoxByValue(By byLocator, String value) {
		WebElement webElement = getDriver().findElement(byLocator);
		Select dropdownList = new Select(webElement);
		dropdownList.selectByValue(value);
	}
	
	public void setSelectBoxByValue(By byLocator, String value, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}		
		Select dropdownList = new Select(webElement);
		dropdownList.selectByValue(value);
	}
	
	public void setSelectBoxByValueWait(By byLocator, String value, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Select dropdownList = new Select(webElement);
		dropdownList.selectByValue(value);
	}
	
	public void setSelectBoxByValueWait(By byLocator, String value, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		Select dropdownList = new Select(webElement);
		dropdownList.selectByValue(value);
	}

	public void setSelectBoxByVisibleTextWait(By byLocator, String visualText, long timeoutInSeconds,
			boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		Select dropdownList = new Select(webElement);
		dropdownList.selectByVisibleText(visualText);
	}

	public void setSelectBoxByVisibleTextWait(By byLocator, String visualText, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		Select dropdownList = new Select(webElement);
		dropdownList.selectByVisibleText(visualText);
	}

	/**
	 * Get the selected value from a standard select box
	 */
	public String getValueSelectBox(WebElement webElement) {
		Select dropdownList = new Select(webElement);
		WebElement selectedOption = dropdownList.getFirstSelectedOption();
		String selectedValue = selectedOption.getText();
		return selectedValue;
	}

	public String getValueSelectBox(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		return getValueSelectBox(webElement);
	}

	public String getValueSelectBoxWait(By byLocator, long defaultTimeoutInSeconds) {
		WebElement webElement = waitUntilPresent(byLocator, defaultTimeoutInSeconds);
		return getValueSelectBox(webElement);
	}

	// 17). Send Key Methods
	/**
	 * 1. sendKeys is the method to pass some content or text into an editable
	 * element without replacing the previous available content.
	 * 
	 * 2. sendKeys is the method to send some predefined key to an click-able element
	 * to simulate user actions such as Enter, Tab, Delete...
	 * 
	 * 3. sendKeys is also work with web page
	 * 
	 * 4. To send multiple key concurrently, use: String multiKeys =
	 * Keys.chord(Keys.ALT, Keys.SHIFT,"z") and pass multiKeys string to the sendKys
	 * method.
	 */
	public void sendKeysToElement(WebElement webElement, Keys key) {
		webElement.sendKeys(key);
	}

	public void sendKeysToElement(By byLocator, Keys key) {
		WebElement webElement = getDriver().findElement(byLocator);
		webElement.sendKeys(key);
	}

	public void sendKeysToElementWait(By byLocator, Keys key, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		webElement.sendKeys(key);
	}

	public void sendKeysToPage(Keys key) {
		Actions actions = new Actions(getDriver());
		actions.sendKeys(key).perform();
	}
	
	public void sendValueToPage(String value) {
		Actions actions = new Actions(getDriver());
		actions.sendKeys(value).perform();
	}

	// 18). Set Value for TextBox Methods
	public void setValueOnlyTextBox(WebElement webElement, String value, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		webElement.sendKeys(value);
	}

	public void setValueOnlyTextBox(By byLocator, String value) {
		WebElement webElement = getDriver().findElement(byLocator);
		webElement.sendKeys(value);
	}

	public void setValueOnlyTextBoxWait(By byLocator, String value, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		webElement.sendKeys(value);
	}

	public void setValueOnlyTextBoxWait(By byLocator, String value, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		webElement.sendKeys(value);
	}

	public void setValueTextBox(WebElement webElement, String value, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		clearTextBox(webElement);
		webElement.sendKeys(value);
	}

	public void setValueTextBox(By byLocator, String value) {
		WebElement webElement = getDriver().findElement(byLocator);
		clearTextBox(webElement);
		webElement.sendKeys(value);
	}

	public void setValueTextBoxWait(By byLocator, String value, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		clearTextBox(webElement);
		webElement.sendKeys(value);
	}

	public void setValueTextBoxWait(By byLocator, String value, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		clearTextBox(webElement);
		webElement.sendKeys(value);
	}

	// Commented these JavaScrip method for now, maybe become needed in future
	// /**
	// * Sets value using JavaScript, it will replace the existing content with the
	// new one
	// * so there is no need to clear it first.
	// */
	// public void setValueJavaScript(WebElement webElement, String value) {
	// JavascriptExecutor js = (JavascriptExecutor) getDriver();
	// js.executeScript("arguments[0].value='" + value + "'", webElement);
	// }
	//
	// public void setValueJavaScript(By byLocator, String value) {
	// WebElement webElement = getDriver().findElement(byLocator);
	// setValueJavaScript(webElement, value);
	// }
	// ===============================================================================//

	// 19). Switch Window Methods
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

	public boolean switchToWindow(String myTitle) {
		String currentWinHandle = null;
		try {
			currentWinHandle = getDriver().getWindowHandle();
		} catch (Exception ex) {
			currentWinHandle = null;
		}
		Set<String> winHandles = getDriver().getWindowHandles();
		try {
			for (String winHandle : winHandles) {
				getDriver().switchTo().window(winHandle);
				String title = getDriver().getTitle();
				if (myTitle.equalsIgnoreCase(title)) {
					return true;
				}
			}
		} catch (Exception ex) {
			// Not found new window, switch back to current window
			if (currentWinHandle != null) {
				getDriver().switchTo().window(currentWinHandle);
			}
			return false;
		}
		if (currentWinHandle != null) {
			getDriver().switchTo().window(currentWinHandle);
		}
		return false;
	}

	// 20). Click Web Element Methods
	public void click(WebElement webElement, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		webElement.click();
	}

	public void click(WebElement webElement) {
		webElement.click();
	}

	public void click(By byLocator, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		click(webElement, scrollIntoViewport);
	}

	public void click(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		click(webElement);
	}

	public void clickWait(By byLocator, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		click(webElement, scrollIntoViewport);
	}

	public void clickWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		click(webElement);
	}

	/**
	 * Clicks using JavascriptExecutor, try not to use JavaScript in UI testing
	 * because it does not simulate the user actions. JavaScript is sent directly to
	 * the DOM so there might be events that are not be fired.
	 */
	public void clickJavaScript(WebElement webElement) {
		JavascriptExecutor js = (JavascriptExecutor) getDriver();
		try {
			js.executeScript("arguments[0].click()", webElement);
		} catch (Exception e) {
			System.out.println(e.getStackTrace());
		}
	}

	public void clickJavaScript(By byLocator, boolean scrollIntoViewport) {
		WebElement webElement = getDriver().findElement(byLocator);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		clickJavaScript(webElement);
	}

	public void clickJavaScript(By byLocator) {
		WebElement webElement = getDriver().findElement(byLocator);
		clickJavaScript(webElement);
	}

	public void clickJavaScriptWait(By byLocator, long timeoutInSeconds, boolean scrollIntoViewport) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		if (scrollIntoViewport) {
			scrollElementIntoViewport(webElement);
		}
		clickJavaScript(webElement);
	}

	public void clickJavaScriptWait(By byLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilClickable(byLocator, timeoutInSeconds);
		clickJavaScript(webElement);
	}

	// 21). Wait Until Methods
	/**
	 * Waits for WebElement defined by the byLocator parameter until it is present
	 * on the web page DOM or the timeout expired (and TimeoutException is thrown).
	 */
	public WebElement waitUntilPresent(By byLocator, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		WebElement webElement = wait.until(ExpectedConditions.presenceOfElementLocated(byLocator));
		return webElement;
	}

	/**
	 * Waits for WebElement defined by the byLocator parameter until it is present
	 * and becomes visible or the timeout expired (and TimeoutException is thrown).
	 * 
	 */
	public WebElement waitUntilVisible(By byLocator, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		WebElement webElement = wait.until(ExpectedConditions.visibilityOfElementLocated(byLocator));
		return webElement;
	}

	/**
	 * Should return the same web element if it becomes visible, otherwise throw
	 * timeout exception
	 */
	public WebElement waitUntilVisible(WebElement webElement, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		WebElement result = wait.until(ExpectedConditions.visibilityOf(webElement));
		return result;
	}

	/**
	 * Waits for WebElement defined by the byLocator parameter until it is present
	 * and becomes visible and enabled or the timeout expired (and TimeoutException
	 * is thrown).
	 * 
	 */
	public WebElement waitUntilClickable(By byLocator, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		WebElement webElement = wait.until(ExpectedConditions.elementToBeClickable(byLocator));
		return webElement;
	}

	/**
	 * Should return the same web element if it becomes clickable, otherwise throw
	 * timeout exception
	 */
	public WebElement waitUntilClickable(WebElement webElement, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		WebElement result = wait.until(ExpectedConditions.elementToBeClickable(webElement));
		return result;
	}

	/**
	 * Waits until this web element disappears or timeout expired and
	 * TimeoutExeception is thrown
	 */
	public void waitUntilDisappear(By selector, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		wait.until(ExpectedConditions.invisibilityOfElementLocated(selector));
	}

	/**
	 * Repeatedly checks if the specified text is present in this element until the
	 * text is present or timeout expired.
	 */
	public boolean waitUntilTextPresent(By byLocator, String text, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		boolean isTextPresent = wait.until(ExpectedConditions.textToBePresentInElementLocated(byLocator, text));
		return isTextPresent;
	}

	public boolean waitUntilTextPresent(WebElement webElement, String text, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		boolean isTextPresent = wait.until(ExpectedConditions.textToBePresentInElement(webElement, text));
		return isTextPresent;
	}

	public List<WebElement> waitUntilAllPresent(By byLocator, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		List<WebElement> webElements = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(byLocator));
		return webElements;
	}

	public List<WebElement> waitUntilAllVisible(By byLocator, long timeoutInSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutInSeconds + GLOBAL_TIMEOUT);
		List<WebElement> webElements = wait.until(ExpectedConditions.visibilityOfAllElementsLocatedBy(byLocator));
		return webElements;
	}

	public Alert waitUntilAlertPresent(long timeoutSeconds) {
		WebDriverWait wait = new WebDriverWait(getDriver(), timeoutSeconds + GLOBAL_TIMEOUT);
		Alert alert = wait.until(ExpectedConditions.alertIsPresent());
		return alert;
	}

	/**
	 * Waits for the loading animation to disappear
	 * 
	 */
	public void waitForLoading(By byBusyAnimation) {
		waitForLoading(byBusyAnimation, 300);
	}

	public void waitForLoading(By byBusyAnimation, long timeoutSeconds) {
		try {
			WebElement webElement1 = getDriver().findElement(byBusyAnimation);
			if (webElement1 != null) {
				waitUntilDisappear(byBusyAnimation, timeoutSeconds);
			}
		} catch (Exception ex) {
		}
	}

	// 22). Get Colors Methods
	/**
	 * Gets color of an element. Color property such as "color", "background-color",
	 * "border-color", "border-top/bottom/left/right-color." The return is a string
	 * of RGBa such as "rgba(0, 255, 0, 1)"
	 */
	public String getBackgroundColor(WebElement webElement) {
		return webElement.getCssValue("background-color");
	}

	public String getColor(WebElement webElement) {
		return webElement.getCssValue("color");
	}

	public String getBorderColor(WebElement webElement) {
		return webElement.getCssValue("border-color");
	}

	public String getBorderTopColor(WebElement webElement) {
		return webElement.getCssValue("border-top-color");
	}

	public String getBorderBottomColor(WebElement webElement) {
		return webElement.getCssValue("border-bottom-color");
	}

	public String getBorderLeftColor(WebElement webElement) {
		return webElement.getCssValue("border-left-color");
	}

	public String getBorderRightColor(WebElement webElement) {
		return webElement.getCssValue("border-right-color");
	}

	// 23). Select Day from Date Picker Methods
	/**
	 * Date picker, changes day only
	 * 
	 * @param byLocator
	 *            the By selector of the date picker
	 * @param value
	 *            the day to pick
	 */
	public void selectDayFromDatePicker(WebElement datePicker, String value, boolean scrollIntoViewport) {
		if (scrollIntoViewport) {
			scrollElementIntoViewport(datePicker);
		}
		List<WebElement> dayCells = datePicker.findElements(By.tagName("td"));
		for (WebElement dayCell : dayCells) {
			if (!isElementClickable(dayCell)) {
				continue;
			}
			String dayText = dayCell.getText();
			if (dayText.equals(value)) {
				try {
					WebElement link = dayCell.findElement(By.linkText(value));
					link.click();
					break;
				} catch (NoSuchElementException ex) {
					continue;
				}
			}
		}
	}

	public void selectDayFromDatePicker(WebElement datePicker, String value) {
		List<WebElement> dayCells = datePicker.findElements(By.tagName("td"));
		for (WebElement dayCell : dayCells) {
			if (!isElementClickable(dayCell)) {
				continue;
			}
			String dayText = dayCell.getText();
			if (dayText.equals(value)) {
				try {
					WebElement link = dayCell.findElement(By.linkText(value));
					link.click();
					break;
				} catch (NoSuchElementException ex) {
					continue;
				}
			}
		}
	}

	public void selectDayFromDatePickerWait(By byLocator, String value, long timeoutInSeconds,
			boolean scrollIntoViewport) {
		WebElement dateDatePicker = waitUntilClickable(byLocator, timeoutInSeconds);
		selectDayFromDatePicker(dateDatePicker, value, scrollIntoViewport);
	}

	public void selectDayFromDatePickerWait(By byLocator, String value, long timeoutInSeconds) {
		WebElement datePicker = waitUntilClickable(byLocator, timeoutInSeconds);
		selectDayFromDatePicker(datePicker, value);
	}

	// 24). Other miscellaneous Methods
	public String getToolTipText(By elementByLocator, By toolTipByLocator) {
		WebElement webElement = getDriver().findElement(elementByLocator);
		Actions actions = new Actions(this.getDriver());
		actions.moveToElement(webElement).click().build().perform();
		WebElement toolTipElement = getDriver().findElement(toolTipByLocator);
		String toolTipText = toolTipElement.getText();
		return toolTipText;
	}

	public String getToolTipText(WebElement webElement, By toolTipByLocator) {
		Actions actions = new Actions(this.getDriver());
		actions.moveToElement(webElement).click().build().perform();
		WebElement toolTipElement = getDriver().findElement(toolTipByLocator);
		String toolTipText = toolTipElement.getText();
		return toolTipText;
	}

	public String getToolTipTextWait(WebElement webElement, By toolTipByLocator, long timeoutInSeconds) {
		Actions actions = new Actions(this.getDriver());
		actions.moveToElement(webElement).click().build().perform();
		WebElement toolTipElement = waitUntilVisible(toolTipByLocator, timeoutInSeconds);
		String toolTipText = toolTipElement.getText();
		return toolTipText;
	}

	public String getToolTipTextWait(By elementByLocator, By toolTipByLocator, long timeoutInSeconds) {
		WebElement webElement = waitUntilVisible(elementByLocator, timeoutInSeconds);
		Actions actions = new Actions(this.getDriver());
		actions.moveToElement(webElement).click().build().perform();
		WebElement toolTipElement = waitUntilVisible(toolTipByLocator, timeoutInSeconds);
		String toolTipText = toolTipElement.getText();
		return toolTipText;
	}

	public static void sleep(long milliSeconds) {
		try {
			Thread.sleep(milliSeconds);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public String getCurrentWebPageTitle() {
		return getDriver().getTitle();
	}

	/**
	 * Navigates the browser to a web page by the specified URL
	 */
	public void navigateToWebPage(String Url) {
		getDriver().get(Url);
	}

	public String getCurrentWebPageUrl() {
		String currentUrl = getDriver().getCurrentUrl();
		return currentUrl;
	}

	public String getCurrentWebPagePath() throws MalformedURLException {
		String currentUrl = getDriver().getCurrentUrl();
		URL url = new URL(currentUrl);
		return url.getPath();
	}

	/**
	 * Equivalent to pressing the back button on browser
	 */
	public void navigateToPreviousWebPage() {
		getDriver().navigate().back();
	}

	public void refreshCurrentWebPage() {
		getDriver().navigate().refresh();
	}
	
	
}
