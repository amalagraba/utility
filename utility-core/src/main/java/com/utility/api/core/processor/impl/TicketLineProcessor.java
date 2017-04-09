package com.utility.api.core.processor.impl;

import com.utility.api.core.processor.ProcessContext;
import com.utility.api.core.processor.RegisterProcessor;
import com.utility.api.core.processor.utils.RegexPattern;
import com.utility.api.entity.TicketLine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@Component
public class TicketLineProcessor implements RegisterProcessor<TicketLine> {

    @Override
    public List<TicketLine> processList(ProcessContext context) {
        String[] rawLines = context.getRawData().split("\\n");
        List<TicketLine> ticketLines = new ArrayList<>(rawLines.length);

        for (String data : rawLines) {
            TicketLine scan = processSingle(new ProcessContext(data));
            if (scan != null) {
                ticketLines.add(scan);
            }
        }
        return ticketLines;
    }

    @Override
    public TicketLine processSingle(ProcessContext context) {
        String data = cleanRawData(context.getRawData());

        if (StringUtils.isNotEmpty(data)) {
            List<String> prices = getPrices(data);

            if (!prices.isEmpty()) {
                for (String price : prices) {
                    data = data.replace(price, StringUtils.EMPTY);
                }
                String name = getName(data);

                data = StringUtils.replace(data, name, StringUtils.EMPTY);

                List<Integer> digits = getDigits(data);
                Integer quantity = getQuantity(digits);
                Integer position = getPosition(digits);

                return adjustAndCreate(name, quantity, position, prices, digits);
            }
        }
        return null;
    }

    private TicketLine adjustAndCreate(String name, Integer quantity, Integer position, List<String> prices, List<Integer> digits) {
        Float price;
        if (prices.size() > 1) {
            Float total = Float.parseFloat(prices.get(1).replace(',', '.'));
            if (quantity == 1) {
                price = Float.parseFloat(prices.get(0).replace(',', '.'));
                quantity = Math.round(total / price);
            } else {
                price = total / quantity;
            }
        } else {
            if (quantity > 1 && digits.size() <= 2) {
                quantity = 1;
            }
            price = Float.parseFloat(prices.get(0).replace(',', '.'));
        }
        BigDecimal bd = new BigDecimal(price);
        bd = bd.setScale(2, BigDecimal.ROUND_HALF_UP);
        price = bd.floatValue();

        return new TicketLine(name, quantity, price, position);
    }

    private String cleanRawData(String rawData) {
        rawData = StringUtils.replaceChars(rawData, "'\"!¡?¿´·-", StringUtils.EMPTY);
        rawData = StringUtils.replaceAll(rawData, " \\. ", " ");
        rawData = StringUtils.replaceAll(rawData, "(?<=\\d)+( *, *)(?=\\d+)", ",");

        return StringUtils.trimToEmpty(rawData);
    }

    private List<String> getPrices(String data) {
        Matcher matcher = RegexPattern.PRICE.matcher(data);
        List<String> prices = new ArrayList<>();

        while (matcher.find()) {
            prices.add(matcher.group());
        }
        return prices;
    }

    private String getName(String data) {
        int numIndex = 0, letterCount = 0;
        char[] characters = data.toCharArray();
        for (int i = 0; i < data.length(); i++) {
            char character = characters[i];
            if (Character.isLetter(character)) {
                letterCount++;
            }
            if (letterCount < 2 && i < characters.length - 1 && characters[i + 1] == ' ' && Character.isDigit(character)) {
                numIndex = i;
            }
        }
        return data.substring(numIndex + 1).trim();
    }

    private List<Integer> getDigits(String data) {
        Matcher matcher = RegexPattern.DIGITS.matcher(data);
        List<Integer> digits = new ArrayList<>();

        while (matcher.find()) {
            digits.add(Integer.parseInt(matcher.group()));
        }
        return digits;
    }

    private Integer getQuantity(List<Integer> digits) {
        if (digits.isEmpty() || digits.size() == 1) {
            return 1;
        } else  {
            return digits.get(digits.size() - 1);
        }
    }

    private Integer getPosition(List<Integer> digits) {
        if (!digits.isEmpty()) {
            return digits.get(0);
        } else {
            return null;
        }
    }
}
