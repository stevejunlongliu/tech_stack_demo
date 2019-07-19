package httpserver.impl;

import org.apache.commons.lang3.StringUtils;

import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Janon on 2014/7/24.
 */
public class UserAgent {
    private static Pattern pattern = Pattern.compile(
            "([^/\\s]*)(/([^\\s]*))?(\\s*\\[[a-zA-Z][a-zA-Z]\\])?\\s*(\\((([^()]|(\\([^()]*\\)))*)\\))?\\s*");
    private static Pattern numberPattern = Pattern.compile("\\d+");

    private static boolean isNumber(String number) {
        return numberPattern.matcher(number).matches();
    }

    public static String[] parser(String userAgentString) {
        if (StringUtils.isEmpty(userAgentString)) return null;

        String appClientId = null;
        String appClientVersion = null;
        String appClientOSVersion = null;
        String appClientDeviceId = null;
        String appClientDeviceName = null;
        String[] s1 = StringUtils.split(userAgentString, "/");//todo 有修改 原版本: StringUtils.split(userAgentString, "/", 2)
        if (s1.length == 2 && s1[0].length() > 3 && isNumber(s1[0])) {
            String browserName = s1[0];
            if (check(browserName)) {
                String[] s = StringUtils.split(s1[1], ";");
                appClientId = browserName;
                appClientVersion = getAppClientVersion(s[0]);
                if (s.length > 1) {
                    appClientOSVersion = s[1];
                }
                //noinspection Duplicates
                for (String ss : s) {

                    if (StringUtils.startsWith(ss, "deviceId:")) {
                        appClientDeviceId = ss.substring(9);
                    } else if (StringUtils.startsWith(ss, "deviceName:")) {
                        try {
                            appClientDeviceName = URLDecoder.decode(ss.substring(11), "UTF-8");
                        } catch (Throwable ignore) {
                            appClientDeviceName = ss.substring(11);
                        }
                    }
                }
            }
        }

        if (StringUtils.isEmpty(appClientId)) {
            Matcher matcher = pattern.matcher(userAgentString);
            while (matcher.find()) {
                String browserName = matcher.group(1);
                String browserVersion = matcher.group(3);
//            String browserComments = null;
//            if (matcher.groupCount() >= 6) {
//                browserComments = matcher.group(6);
//            }
                if (StringUtils.isEmpty(browserName) || browserName.length() < 3)
                    continue;
//                if (!StringUtils.contains(browserVersion, ";")) continue;

                // 旧格式 appclientid/version;os;brand;model
                // 新格式 appclientid/version;os;brand;model;appid/instanceName
                if (check(browserName)) {
                    String[] s = StringUtils.split(browserVersion, ";");
                    appClientId = browserName;
                    appClientVersion = getAppClientVersion(s[0]);
                    if (s.length > 1) {
                        appClientOSVersion = s[1];
                    }
                    //noinspection Duplicates
                    for (String ss : s) {
                        if (StringUtils.startsWith(ss, "deviceId:")) {
                            appClientDeviceId = ss.substring(9);
                        } else if (StringUtils.startsWith(ss, "deviceName:")) {
                            try {
                                appClientDeviceName = URLDecoder.decode(ss.substring(11), "UTF-8");
                            } catch (Throwable ignore) {
                                appClientDeviceName = ss.substring(11);
                            }
                        }
                    }
                    break;
                }
            }
        }

        if (StringUtils.isEmpty(appClientId)) {
            return null;
        } else {
            if ("10204".equals(appClientId) && StringUtils.isNotEmpty(appClientOSVersion)) {
                appClientVersion = getAppClientVersion(appClientOSVersion);
                appClientOSVersion = null;
            }
            return new String[]{appClientId, appClientVersion, appClientDeviceId, appClientDeviceName, appClientOSVersion};
        }
    }

    private static String getAppClientVersion(String s) {
        if (s.contains(" ")) {
            String[] s2 = s.split(" ", 2);
            if (s2.length > 0) {
                return s2[0];
            }
        }
        return s;
    }

    private static boolean check(String browserName) {
        if (isNumber(browserName) && browserName.length() == 5 &&
                (StringUtils.startsWith(browserName, "1")//1开头公有云
                        || StringUtils.startsWith(browserName, "3") //3开头专有云
                )) {
//        if (Constants.CLIENT_WEB.equals(browserName) || Constants.CLIENT_DESKTOP.equals(browserName) || EcLiteUtil.isMobileClientId(browserName)) {
            return true;
        }
        return false;
    }
}
