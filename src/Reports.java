import db.DBaseElement;
import javafx.util.converter.LocalDateStringConverter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.util.CellRangeAddress;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class Reports{
    public static void consolidatedReport(LocalDate begin, LocalDate end){
        if(begin == null) begin = LocalDate.of(1970,1,1);
        if(end == null) end = LocalDate.now();

        int i=1;
        File xlsFile;
        do{
            xlsFile = new File( System.getProperty("user.home")+System.getProperty("file.separator")+"Report"+Integer.toString(i)+".xlsx");
            i++;
        }while (xlsFile.exists());

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd.MM.yyyy");
        LocalDateStringConverter converter = new LocalDateStringConverter(format, format);

        Workbook book = new HSSFWorkbook();
        Sheet sheet = book.createSheet("report");
        Row row = sheet.createRow(1);
        row.setHeightInPoints((short)13);

        sheet.setColumnWidth(1, 10000);
        sheet.setColumnWidth(2, 5000);

        //Заоголовок
        Cell cell = row.createCell(1);
        cell.setCellValue("Общие итоги за период:");
        CellStyle cs = book.createCellStyle();
        Font font= book.createFont();
        font.setFontHeightInPoints((short)14);
        font.setBold(true);
        cs.setFont(font);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cs.setVerticalAlignment(VerticalAlignment.CENTER);
        cell.setCellStyle(cs);

        cell = row.createCell(2);
        cell.setCellValue(converter.toString(begin));
        cs = book.createCellStyle();
        cs.setAlignment(HorizontalAlignment.LEFT);
        cell.setCellStyle(cs);

        row = sheet.createRow(2);
        row.setHeightInPoints(20);

        cell = row.createCell(2);
        cell.setCellValue(converter.toString(end));

        cs = book.createCellStyle();
        cs.setAlignment(HorizontalAlignment.RIGHT);
        cell.setCellStyle(cs);

        //Шапка
        row = sheet.createRow(6);
        cell = row.createCell(1);
        cell.setCellValue("Клиенты");
        cs = book.createCellStyle();
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cs);

        cell = row.createCell(2);
        cell.setCellValue("Часы");
        cs = book.createCellStyle();
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        cell.setCellStyle(cs);
        cell = row.createCell(3);
        cs = book.createCellStyle();
        cs.setBorderBottom(BorderStyle.THIN);
        cell.setCellStyle(cs);

        CellRangeAddress rangeAddress = new CellRangeAddress(1,2,1,1);
        sheet.addMergedRegion(rangeAddress);

        BigDecimal result = new BigDecimal("0.0");
        //СТРОКИ
        int iRow = 7;
        try{

            try(PreparedStatement statement = DBaseElement.getDbConnection().prepareStatement("SELECT CLIENTS.FULL_NAME, SUM(WORKS.AMOUNT) FROM WORKS LEFT JOIN CLIENTS ON WORKS.CLIENT_ID = CLIENTS.ID WHERE WORKS.WORK_DATE BETWEEN ? AND ? GROUP BY CLIENTS.FULL_NAME ORDER BY CLIENTS.FULL_NAME")){
                statement.setDate(1,Date.valueOf(begin));
                statement.setDate(2,Date.valueOf(end));
                ResultSet resultSet= statement.executeQuery();
                while (resultSet.next()){
                    String client = resultSet.getString(1);
                    if(client == null) client = "";
                    else client = client.trim();
                    BigDecimal bd = resultSet.getBigDecimal(2);
                    row = sheet.createRow(iRow++);
                    cell = row.createCell(1);
                    cell.setCellValue(client);
                    cs = book.createCellStyle();
                    cs.setBorderRight(BorderStyle.THIN);
                    cell.setCellStyle(cs);

                    cell = row.createCell(2);
                    cell.setCellValue(bd.toString());
                    cs = book.createCellStyle();
                    cs.setAlignment(HorizontalAlignment.RIGHT);
                    cs.setBorderRight(BorderStyle.THIN);
                    cell.setCellStyle(cs);

                    result = result.add(bd);
                }

            }

        }catch (SQLException e){
            e.printStackTrace();
        }

        //ПОДВАЛ
        Font uderFont= book.createFont();
        uderFont.setBold(true);

        row = sheet.createRow(iRow++);
        row = sheet.createRow(iRow++);
        cell = row.createCell(1);
        cs = book.createCellStyle();
        cs.setFont(uderFont);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.RIGHT);
        cell.setCellStyle(cs);
        cell.setCellValue("Итого:");

        cell = row.createCell(2);
        cs = book.createCellStyle();
        cs.setFont(uderFont);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.RIGHT);
        cell.setCellStyle(cs);
        cell.setCellValue(result.toString());

        cell = row.createCell(3);
        cs = book.createCellStyle();
        cs.setBorderTop(BorderStyle.THIN);
        cell.setCellStyle(cs);


        try{
            try(OutputStream os = new FileOutputStream(xlsFile.getAbsolutePath())) {
                book.write(os);
                book.close();
            }
            if(xlsFile.exists()) {
                File file = new File(xlsFile.getAbsolutePath());
                new Thread(() -> {
                    try {
                        Desktop.getDesktop().open(file);
                    } catch (IOException e) {
                    }
                    ;
                }).start();
            }
        }catch (IOException e){
            e.printStackTrace();
        }

    }

}
