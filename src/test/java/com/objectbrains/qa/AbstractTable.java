package com.objectbrains.qa;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

public class AbstractTable {
	private WebElement table = null;
	private int tableRowCount = 0;
	private int tableColumnCount = 0;
	private WebDriver driver;


	public AbstractTable(WebDriver driver) {
		this.driver = driver;
	}
	
	public static void sleep(long milliSeconds){
		try{
			Thread.sleep(milliSeconds);
		}catch(InterruptedException e){
			e.printStackTrace();
		}
	}		
	
	public void initialize(By bySelector) throws NoSuchElementException{
		try{
			table = driver.findElement(bySelector);
			tableRowCount = countRows();
			tableColumnCount = countColumns();
		}catch(NoSuchElementException e){
			throw e;
		}catch(Exception e){
			throw e;
		}
	}
	
	public void initialize(WebElement table){
		this.table = table;
		tableRowCount = countRows();
		tableColumnCount = countColumns();
	}
	
	public void refresh(){
		tableRowCount = countRows();
		tableColumnCount = countColumns();
	}
	
	public int getRowCount() {
		return tableRowCount;
	}
	
	public int getColumnCount() {
		return tableColumnCount;
	}	
		
	private int countRows() {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));
			sleep(2000);
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));
			sleep(2000);
			int count = rows.size();
			return count;
		}catch(NoSuchElementException e){
			return 0;
		}
	}
	
/*	private int countColumns() {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
			int count = headerCells.size();
			return count;
		}catch(NoSuchElementException e){
			return 0;
		}
	}*/
	
	private int countColumns() {
		if (tableRowCount == 0) {
			return 0;
		}
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));
			sleep(2000);
			WebElement tableRow = tableBody.findElement(By.tagName("tr"));
			List<WebElement> rowCells = tableRow.findElements(By.tagName("td"));
			int count = rowCells.size();
			return count;
		}catch(NoSuchElementException e){
			return 0;
		}
	}	
	
	
	public WebElement getHeaderRow() {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			return headerRow;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public WebElement getHeaderCell(int columnIndex) {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
			WebElement result = headerCells.get(columnIndex);
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<WebElement> getAllHeaderCells() {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
			return headerCells;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public String getHeaderText(int columnIndex) {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
			WebElement headerCell = headerCells.get(columnIndex);
			return headerCell.getText();
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<String> getAllHeaderTexts() {
		try{
			WebElement tableHead = table.findElement(By.tagName("thead"));		
			WebElement headerRow = tableHead.findElement(By.tagName("tr"));
			List<WebElement> headerCells = headerRow.findElements(By.tagName("th"));
			List<String> headerTexts = new ArrayList<String>();
			for (WebElement cell : headerCells) {
				headerTexts.add(cell.getText());
			}
			return headerTexts;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public WebElement getRow(int index) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> dataRows = tableBody.findElements(By.tagName("tr"));
			WebElement result = dataRows.get(index);
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<WebElement> getAllCellsInRow(int rowIndex) {
		WebElement row = getRow(rowIndex);
		List<WebElement> cells = row.findElements(By.tagName("td"));
		return cells;	
	}		
	
	public List<WebElement> getAllCellsInColumn(int columnIndex) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{					
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("td"));
						cellsInColumn.add(cells.get(columnIndex));
					}
					return cellsInColumn;											
				}			
			}
		}catch(NoSuchElementException e){
			return null;
		}
	}	
		
	public WebElement getCell(int rowIndex, int columnIndex) {
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{
				int rowCount = rows.size();
				if (rowIndex >= rowCount){
					return null;
				}else{
					List<WebElement> cells = rows.get(rowIndex).findElements(By.tagName("td"));
					if (cells == null || cells.isEmpty()){
						return null;					
					}else{
						int cellCount = cells.size();
						if (columnIndex >= cellCount){
							return null;
						}else{
							return cells.get(columnIndex);
						}						
					}
				}
			}
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public String getCellText(int rowIndex, int columnIndex) {
		return getCell(rowIndex, columnIndex).getText();
	}
	
	public String getRowText(int rowIndex) {
		String value = null;
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return value;
			}
			else{
				int rowCount = rows.size();
				if (rowIndex >= rowCount){
					return value;
				}else{
					List<WebElement> cells = rows.get(rowIndex).findElements(By.tagName("td"));
					if (cells == null || cells.isEmpty()){
						return value;					
					}else{
						for (WebElement cell: cells){
							if (cell != null){
								String temp = cell.getText().trim();
								if (temp != null && !temp.isEmpty()){
									value += temp;
								}
							}
						}
						return value;
					}
				}
			}
		}catch(NoSuchElementException e){
			return value;
		}
	}
	
	public WebElement findCellInColumnWithText(int columnIndex, String value, boolean ignoreCase){
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{					
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("td"));
						WebElement cell = cells.get(columnIndex);
						String cellText = cell.getText();
						if (cellText != null){
							if (ignoreCase){
								if (cellText.equalsIgnoreCase(value)){
									return cell;
								}
							}else{
								if (cellText.equals(value)){
									return cell;
								}
							}
						}
					}
					return null;											
				}			
			}
		}catch(NoSuchElementException e){
			return null;
		}		
	}	
	
	public List<WebElement> findAllCellsInColumnWithText(int columnIndex, String value, boolean ignoreCase){
		List<WebElement> foundCells = new ArrayList<WebElement>();					
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{	
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("td"));
						WebElement cell = cells.get(columnIndex);
						String cellText = cell.getText();
						if (cellText != null){
							if (ignoreCase){
								if (cellText.equalsIgnoreCase(value)){
									foundCells.add(cell);
								}
							}else{
								if (cellText.equals(value)){
									foundCells.add(cell);
								}
							}
						}
					}
				}			
			}
		}catch(NoSuchElementException e){
			return null;
		}
		return foundCells;
	}	
		
	public WebElement findCellInColumnContainsText(int columnIndex, String value, boolean ignoreCase, int maxRows){
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{					
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						if (maxRows > 0 && i >= maxRows){
							break;
						}
						cells = rows.get(i).findElements(By.tagName("td"));
						WebElement cell = cells.get(columnIndex);
						String cellText = cell.getText();
						if (cellText != null){
							if (ignoreCase){
								if (cellText.toLowerCase().contains(value.toLowerCase())){
									return cell;
								}
							}else{
								if (cellText.contains(value)){
									return cell;
								}
							}
						}
					}
					return null;											
				}			
			}
		}catch(NoSuchElementException e){
			return null;
		}		
	}	
	
	public List<WebElement> findAllCellsInColumContainText(int columnIndex, String value, boolean ignoreCase, int maxRows){
		List<WebElement> foundCells = new ArrayList<WebElement>();					
		try{
			WebElement tableBody = table.findElement(By.tagName("tbody"));		
			List<WebElement> rows = tableBody.findElements(By.tagName("tr"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("td"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{	
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						if (maxRows > 0 && i >= maxRows){
							break;
						}						
						cells = rows.get(i).findElements(By.tagName("td"));
						WebElement cell = cells.get(columnIndex);
						String cellText = cell.getText();
						if (cellText != null){
							if (ignoreCase){
								if (cellText.toLowerCase().contains(value.toLowerCase())){
									foundCells.add(cell);
								}
							}else{
								if (cellText.contains(value)){
									foundCells.add(cell);
								}
							}
						}
					}
				}			
			}
		}catch(NoSuchElementException e){
			return null;
		}
		return foundCells;
	}		
}
