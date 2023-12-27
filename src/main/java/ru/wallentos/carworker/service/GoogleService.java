package ru.wallentos.carworker.service;


import com.google.api.client.googleapis.json.GoogleJsonError;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.AppendValuesResponse;
import com.google.api.services.sheets.v4.model.ValueRange;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.wallentos.carworker.configuration.ConfigDataPool;
import ru.wallentos.carworker.model.Province;

@Service
@Slf4j
public class GoogleService {
    @Autowired
    private ConfigDataPool configDataPool;
    private Sheets sheets;

    @Autowired
    public GoogleService(Sheets sheets) {
        this.sheets = sheets;
    }


    /**
     * Returns a range of values from a spreadsheet.
     *
     * @param spreadsheetId - Id of the spreadsheet.
     * @return Values in the range
     * @throws IOException - if credentials file not found.
     */
    public ValueRange getValues(String spreadsheetId) throws IOException {
        ValueRange result = null;
        try {
            // Gets the values of the cells in the specified range.
            var res = sheets.spreadsheets().get(spreadsheetId).execute();
            int numRows = result.getValues() != null ? result.getValues().size() : 0;
            System.out.printf("%d rows retrieved.", numRows);
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Returns a range of values from a spreadsheet.
     *
     * @param spreadsheetId - Id of the spreadsheet.
     * @return Values in the range
     * @throws IOException - if credentials file not found.
     */
    private int getCurrentIndexInClientRequestList(String spreadsheetId) {
        ValueRange result = null;
        try {
            // Gets the values of the cells in the specified range.
            result =
                    sheets.spreadsheets().values().get(spreadsheetId, "Общая таблица!A1:A").execute();
            int numRows = result.getValues().size();
            if (numRows == 1) {
                return 0;
            }
            try {
                return Integer.parseInt(result.getValues().get(numRows - 1).get(0).toString());
            } catch (NumberFormatException e) {
                return Integer.parseInt(result.getValues().get(numRows - 1).get(0).toString().substring(1));
            }
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return 0;
    }

    /**
     * Получаем из гугл таблицы данные по доставкам, преобразовываем в мапу ПРОВИНЦИЯ-ЮАНИ
     *
     * @return Values in the range
     * @throws IOException - if credentials file not found.
     */
    public Map<String, Province> getManagerLogisticsProvinceMap() {
        Map<String, Province> result = new HashMap<>();
        try {
            // Gets the values of the cells in the specified range.
            var selection =
                    sheets.spreadsheets().values().get(configDataPool.managerLogisticsSpreedSheetId, "last!C3:H").execute();
            var values = selection.getValues();
            values.forEach(data -> {
                        try {
                            result.put(data.get(1).toString(),
                                    new Province(data.get(0).toString(),
                                            Integer.parseInt(data.get(5).toString().replace("¥",
                                                    "").replace(" ", "")),
                                            Integer.parseInt(data.get(3).toString().replace("¥", "").replace(" ",
                                                    ""))
                                    ));
                        } catch (RuntimeException e) {
                            log.warn("не удалось считать строку из гугла. {}", e.getMessage());
                        }
                    }
            );
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", configDataPool.managerLogisticsSpreedSheetId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    /**
     * Appends values to a spreadsheet.
     *
     * @param spreadsheetId    - Id of the spreadsheet.
     * @param range            - Range of cells of the spreadsheet.
     * @param valueInputOption - Determines how input data should be interpreted.
     * @param values           - list of rows of values to input.
     * @return spreadsheet with appended values
     * @throws IOException - if credentials file not found.
     */
    private AppendValuesResponse appendValues(String spreadsheetId,
                                              String range,
                                              String valueInputOption,
                                              List<List<Object>> values)
            throws IOException {
        AppendValuesResponse result = null;
        try {
            // Append values to the specified range.
            ValueRange body = new ValueRange()
                    .setValues(values);
            result = sheets.spreadsheets().values().append(spreadsheetId, range, body)
                    .setValueInputOption(valueInputOption)
                    .execute();
            // Prints the spreadsheet with appended values.
            System.out.printf("%d cells appended.", result.getUpdates().getUpdatedCells());
        } catch (GoogleJsonResponseException e) {
            // TODO(developer) - handle error appropriately
            GoogleJsonError error = e.getDetails();
            if (error.getCode() == 404) {
                System.out.printf("Spreadsheet not found with id '%s'.\n", spreadsheetId);
            } else {
                throw e;
            }
        }
        return result;
    }

    /**
     * Добавить заявку клиента в Гугл таблицу
     *
     * @param clientRequestText
     * @param clientUserName
     */
    public void appendClientRequestToGoogleSheet(String clientRequestText, String clientUserName) {
        int newIndex = getCurrentIndexInClientRequestList(configDataPool.getClientRequestSpreedSheetId()) + 1;
        List<List<Object>> inputValues = Arrays.asList(Arrays.asList(
                newIndex, LocalDate.now(ZoneId.of("UTC+3")).toString(),
                LocalDateTime.now(ZoneId.of("UTC+3")).format(DateTimeFormatter.ofPattern("HH:mm:ss")),
                clientRequestText, "https://t.me/" + clientUserName));
        try {
            appendValues(configDataPool.getClientRequestSpreedSheetId(), "Общая таблица!A2:E",
                    "USER_ENTERED", inputValues);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


}