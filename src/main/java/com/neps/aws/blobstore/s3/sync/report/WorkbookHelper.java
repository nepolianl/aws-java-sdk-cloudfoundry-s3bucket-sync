package com.neps.aws.blobstore.s3.sync.report;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;

import com.neps.aws.blobstore.s3.sync.model.CopyS3Object;
import com.neps.aws.blobstore.s3.sync.util.Utils;

@Component
public class WorkbookHelper {
	private static final Logger logger = Logger.getLogger(WorkbookHelper.class.getName());
	private static String[] blobColumns = {"File", "Last Modified", "Size (Bytes)", "Size (Readable)"};
	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
	private final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss.SSSXXX");
	
	public File getAttachment(List<CopyS3Object> copyList) throws IOException {
		File file = File.createTempFile("Backup-"+ dateFormat.format(new Date())+"-", ".xlsx");
		
		try (Workbook workbook = new XSSFWorkbook(); FileOutputStream  fileOut = new FileOutputStream(file);) {
			// Create a Sheet
			Sheet sheet = workbook.createSheet("Backup-"+ dateFormat.format(new Date()));
			
			// Create a Font for styling header cells
			Font headerFont = workbook.createFont();
			headerFont.setFontHeightInPoints((short) 14);
			headerFont.setColor(IndexedColors.BLACK.getIndex());

			// Create a CellStyle with the font
			CellStyle headerCellStyle = workbook.createCellStyle();
			headerCellStyle.setFont(headerFont);

			// Create a Row
			Row headerRow = sheet.createRow(0);
			
			// Create cells
			for (int i = 0; i < blobColumns.length; i++) {
				Cell cell = headerRow.createCell(i);
				cell.setCellValue(blobColumns[i]);
				cell.setCellStyle(headerCellStyle);
			}

			// Create Other rows and cells with employees data
			int rowNum = 1;
			for (CopyS3Object s3Object : copyList) {
				Row row = sheet.createRow(rowNum++);

				row.createCell(0).setCellValue(s3Object.getKey());
				row.createCell(1).setCellValue(dateTimeFormat.format(s3Object.getLastModified()));
				row.createCell(2).setCellValue(s3Object.getFileSize());
				row.createCell(3).setCellValue(Utils.toHumanReadable(s3Object.getFileSize()));
			}

			// Resize all columns to fit the content size
			for (int i = 0; i < blobColumns.length; i++) {
				sheet.autoSizeColumn(i);
			}
			
			workbook.write(fileOut);
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Could not write copyObject list to workbook", e);
		}
		
		return file;
	}

}
