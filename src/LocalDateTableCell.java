import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.LocalDateStringConverter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateTableCell<S> extends TableCell<S,LocalDate> {
    LocalDateTableCell() {
        DateTimeFormatter formatter =DateTimeFormatter.ofPattern("dd.MM.yyyy");
        converter = new LocalDateStringConverter(formatter, formatter);
    }

    public static <S> Callback<TableColumn<S,LocalDate>,TableCell<S,LocalDate>> getCellFactory(){
        return param -> new LocalDateTableCell<>();
    }
    @Override
    protected void updateItem(LocalDate item, boolean empty) {
        super.updateItem(item, empty);
        setText(converter.toString(item));
    }
    private StringConverter<LocalDate> converter;
}
