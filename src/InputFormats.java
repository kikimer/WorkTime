import javafx.scene.control.TextFormatter;

public class InputFormats {


    public static TextFormatter.Change formatTime(TextFormatter.Change change) {

        int start = change.getRangeStart();
        int end = change.getRangeEnd();
        int controlLength = change.getControlText().length();
        int textLength = change.getText().length();

        boolean isAdded = change.isAdded();
        boolean isDeleted = change.isDeleted();
        boolean isReplaced = change.isReplaced();

        //корректировки вводимых сиволов
        if(isReplaced) {
            //ничего не далем.
        }else if(isAdded){ //замена вместо вставки

            if(start ==2){
                start++; //замена символа ':' приравнивается к замене со следующего символа
                int carPos = change.getCaretPosition();
                change.setCaretPosition(carPos+1);
                change.setAnchor(carPos+1);
            }
            change.setRange(start, Math.min(controlLength, start+textLength));

        }else if(isDeleted){//вставка '0' вместо удаления

            String delText = change.getControlText().substring(start, end);
            int countTimeCharacter = removeNonDigitSymbols(delText).length();
            change.setText("0000".substring(0, Math.min(4,countTimeCharacter)));
        }

        String result = removeNonDigitSymbols(change.getControlNewText());
        result = (result+"0000").substring(0,4);
        result = result.substring(0,2)+":"+result.substring(2,4);
        change.setRange(0, change.getControlText().length());
        change.setText(result);



        return change;
    }

    public static TextFormatter.Change formatDigit(TextFormatter.Change change) {

        String result = change.getControlNewText()+".0";
        //один сивол после запятой.
        int dotIndex = result.indexOf('.');
        if(dotIndex == result.length()-1)
            result = result+"0";
        else if(dotIndex == -1)
            result = result+".0";

        StringBuilder sb = new StringBuilder();
        for(int i=0; i <= dotIndex+1; i++){
            char c = result.charAt(i);
            if(Character.isDigit(c) || c=='.') sb.append(c);
        }
        result = sb.toString();

        change.setRange(0, change.getControlText().length());
        change.setText(result);
        return change;
    }

    private static String removeNonDigitSymbols(String text){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i < text.length(); i++){
            char c = text.charAt(i);
            if(Character.isDigit(c)) sb.append(c);
        }
        return sb.toString();
    }
}
