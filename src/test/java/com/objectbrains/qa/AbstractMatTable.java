package com.objectbrains.qa;

import java.util.ArrayList;
import java.util.List;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;


public class AbstractMatTable {
	
	private WebElement table = null;
	private int tableRowCount = 0;
	private int tableColumnCount = 0;
	private WebDriver driver;

	
	public AbstractMatTable(WebDriver driver) {
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));
			sleep(2000);
			int count = rows.size();
			return count;
		}catch(NoSuchElementException e){
			return 0;
		}
	}
	
/*	private int countColumns() {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			List<WebElement> headerCells = headerRow.findElements(By.tagName("mat-header-cell"));
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
			WebElement dataRow = table.findElement(By.tagName("mat-row"));
			List<WebElement> dataCells = dataRow.findElements(By.tagName("mat-cell"));
			int count = dataCells.size();
			return count;
		}catch(NoSuchElementException e){
			return 0;
		}
	}	
	
	public WebElement getHeaderRow() {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			return headerRow;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public WebElement getHeaderCell(int columnIndex) {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			List<WebElement> headerCells = headerRow.findElements(By.tagName("mat-header-cell"));
			WebElement result = headerCells.get(columnIndex);
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<WebElement> getAllHeaderCells() {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			List<WebElement> headerCells = headerRow.findElements(By.tagName("mat-header-cell"));
			return headerCells;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public String getHeaderText(int columnIndex) {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			List<WebElement> headerCells = headerRow.findElements(By.tagName("mat-header-cell"));
			WebElement headerCell = headerCells.get(columnIndex);
			return headerCell.getText();
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<String> getAllHeaderTexts() {
		try{
			WebElement headerRow = table.findElement(By.tagName("mat-header-row"));		
			List<WebElement> headerCells = headerRow.findElements(By.tagName("mat-header-cell"));
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
			List<WebElement> dataRows = table.findElements(By.tagName("mat-row"));
			WebElement result = dataRows.get(index);
			return result;
		}catch(NoSuchElementException e){
			return null;
		}
	}
	
	public List<WebElement> getAllCellsInRow(int rowIndex) {
		WebElement row = getRow(rowIndex);
		List<WebElement> cells = row.findElements(By.tagName("mat-cell"));
		return cells;	
	}		
	
	public List<WebElement> getAllCellsInColumn(int columnIndex) {
		try{
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("mat-cell"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{					
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("mat-cell"));
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{
				int rowCount = rows.size();
				if (rowIndex > rowCount-1 || rowIndex < 0){
					return null;
				}else{
					List<WebElement> cells = rows.get(rowIndex).findElements(By.tagName("mat-cell"));
					if (cells == null || cells.isEmpty()){
						return null;					
					}else{
						int cellCount = cells.size();
						if (columnIndex > cellCount){
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return value;
			}
			else{
				int rowCount = rows.size();
				if (rowIndex > rowCount-1 || rowIndex < 0){
					return value;
				}else{
					List<WebElement> cells = rows.get(rowIndex).findElements(By.tagName("mat-cell"));
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("mat-cell"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{					
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("mat-cell"));
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("mat-cell"));
				if (cells == null || cells.isEmpty()){
					return null;					
				}else{	
					List<WebElement> cellsInColumn = new ArrayList<WebElement>();
					cellsInColumn.add(cells.get(columnIndex));
					int rowCount = rows.size();
					for (int i = 1; i< rowCount ; i++) {
						cells = rows.get(i).findElements(By.tagName("mat-cell"));
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("mat-cell"));
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
						cells = rows.get(i).findElements(By.tagName("mat-cell"));
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
			List<WebElement> rows = table.findElements(By.tagName("mat-row"));			
			if (rows == null || rows.isEmpty()){
				return null;
			}
			else{				
				List<WebElement> cells = rows.get(0).findElements(By.tagName("mat-cell"));
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
						cells = rows.get(i).findElements(By.tagName("mat-cell"));
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
