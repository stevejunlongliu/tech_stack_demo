package httpserver.util;

import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AcceptLanguageParser {

    public static List<Locale> parse(String header) {
        List<Locale> rtn = new ArrayList<Locale>();
        for (String str : header.split(",")) {
            String[] arr = str.trim().replace("-", "_").split(";");

            Locale locale = null;
            String[] l = arr[0].split("_");
            switch (l.length) {
                case 2:
                    locale = new Locale(l[0], l[1]);
                    break;
                case 3:
                    locale = new Locale(l[0], l[1], l[2]);
                    break;
                default:
                    locale = new Locale(l[0]);
                    break;
            }
            rtn.add(locale);
        }
        return rtn;
    }

    public static Locale getLocale(String header, Locale defaultLocale) {
        if (StringUtils.isEmpty(header)) return defaultLocale;
        List<Locale> list = parse(header);
        if (list.size() == 0 || list.get(0) == null) return defaultLocale;
        return list.get(0);
    }
}
