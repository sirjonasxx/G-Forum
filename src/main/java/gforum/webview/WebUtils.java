package gforum.webview;

import org.apache.commons.lang3.time.DateUtils;
import org.w3c.dom.Element;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class WebUtils {

    public static String elapsedTime(int time) {
        if (time < 60) return time + (time == 1 ? " second" : " seconds");
        time = time/60;
        if (time < 60) return time + (time == 1 ? " minute" : " minutes");
        time = time/60;
        if (time < 24) return time + (time == 1 ? " hour" : " hours");
        int days = time/24;
        if (days < 7) return days + (days == 1 ? " day" : " days");
        int weeks = days/7;
        if (weeks < 4) return weeks + (weeks == 1 ? " week" : " weeks");
        int months = days/30;
        if (months < 12) return months + (months == 1 ? " month" : " months");
        int years = days/365;
        return years + (years == 1 ? " year" : " years");
    }

    public static String escapeMessage(String text) {
        return text
                .replace("\n\r", "<br />")
                .replace("\n", "<br />")
                .replace("\r", "<br />")
                .replace("'", "\\'");
    }

    public static void clearElement(Element node) {
        while (node.hasChildNodes())
            node.removeChild(node.getFirstChild());
    }

    public static void removeClass(Element node, String classname) {
        String[] classes = node.getAttribute("class").split(" ");
        List<String> classesFiltered = new ArrayList<>(Arrays.asList(classes));
        classesFiltered = classesFiltered.stream().filter(s -> !s.toLowerCase().equals(classname.toLowerCase())).collect(Collectors.toList());
        node.setAttribute("class", String.join(" ", classesFiltered));
    }

    public static void addClass(Element node, String classname) {
        removeClass(node, classname);
        node.setAttribute("class", node.getAttribute("class") + " " + classname);
    }

}
