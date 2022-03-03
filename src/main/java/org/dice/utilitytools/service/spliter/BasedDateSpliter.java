package org.dice.utilitytools.service.spliter;

import org.springframework.stereotype.Component;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;

@Component
public class BasedDateSpliter {
    private List<String> beforeList;
    private List<String> afterList;

    public List<String> getBeforeList() {
        return beforeList;
    }

    public List<String> getAfterList() {
        return afterList;
    }

    public BasedDateSpliter(){
        beforeList = new ArrayList<>();
        afterList = new ArrayList<>();
    }

    public void split(String filePath, String splitDate, String separatorCharacter) {
        System.out.println("start splitting");
    try{
        File file = new File(filePath);
        Date thresholdDate = toDate(splitDate);
        Scanner scanner = new Scanner(file);
        String line;
        // skip the first line
        scanner.nextLine();
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            //System.out.println(line);
            //System.out.println(separatorCharacter);
            String dateInStringFormat = line.split(separatorCharacter)[2];
            Date date = convertDatePartToDate(dateInStringFormat);
            if(date.before(thresholdDate)){
                // add to befor
                beforeList.add(line);
            }else {
                // add to after
                afterList.add(line);
            }
        }
        scanner.close();
    }catch (Exception e){
        System.out.println(e.getMessage());
    }
    }

    private Date convertDatePartToDate(String dateInStringFormat) throws ParseException {
        // the input is a String like this "2009-03"^^<http://www.w3.org/2001/XMLSchema#gYearMonth>
        dateInStringFormat = dateInStringFormat.replace("\"","");
        String[] parts = dateInStringFormat.split("\\^");
        System.out.println(parts.length);
        String date_string = parts[0]+"-01";
        Date convertedDate = toDate(date_string);
        return convertedDate;
    }

    private Date toDate(String input) throws ParseException {
        System.out.println(input);
        //Instantiating the SimpleDateFormat class
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        //Parsing the given String to Date object
        Date convertedDate = formatter.parse(input);
        return convertedDate;
    }
}
