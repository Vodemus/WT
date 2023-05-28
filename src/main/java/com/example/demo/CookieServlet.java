package com.example.demo;

import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.WebServlet;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.nio.file.*;
import java.util.*;

@WebServlet(name = "CookieServlet", value = "/hello-servlet")
public class CookieServlet extends HttpServlet {
    private static final String VISITORS_FILE_PATH = "C:\\Users\\Максим\\IdeaProjects\\demo\\visitors.txt";
    private static final String UNIQ_ID = "lastVisit";
    private static final String USER = "user";
    private static final AtomicInteger counter = new AtomicInteger();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        var session = req.getSession();
        UserDto user;
        user = (UserDto) session.getAttribute(USER);
        if (user == null) {
            user = new UserDto(25L, "test@gmail.com");
            session.setAttribute(USER, user);
        }


        var browser = getBrowser(req.getHeader("User-Agent"));
        var ipAddress = req.getRemoteAddr();
        var lastVisitDate = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        var cookies = req.getCookies();
        var writer = resp.getWriter();
        resp.setContentType("text/html");

        if (Arrays.stream(cookies)
                .filter(cookie -> UNIQ_ID.equals(cookie.getName()))
                .findFirst()
                .isEmpty()) {
            var cookie = new Cookie(UNIQ_ID, lastVisitDate);
            cookie.setMaxAge(-1);
            resp.addCookie(cookie);
            counter.incrementAndGet();

            writer.write("<h1>Welcome to the website!</h1>");
            var pathToFile = Paths.get(VISITORS_FILE_PATH);
            Files.writeString(pathToFile, lastVisitDate+" "+ipAddress+" "+browser+"\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        else {
            writer.write("<h1>Welcome back to the website!</h1>");
            String cookieValue;
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(UNIQ_ID)) {
                    cookieValue = cookie.getValue();
                    cookie.setValue(lastVisitDate);
                    writer.write("<p>Last visit date: " + cookieValue + ".</p>");
                    writer.write("<p>Browser: " + browser + ".</p>");
                    break;
                }
            }
        }

        writer.write("<h1> Number of unique visitors:" + counter.get() + "</h1>");

        System.out.println("HttpServletRequest Attributes -----------------");
        printAttributes(req);
        System.out.println("ServletContext Attributes -----------------");
        printAttributes(req.getServletContext());
        System.out.println("HttpSession Attributes ---------------");
        printAttributes(req.getSession());
    }

    private static String getBrowser(String userAgent) {
        if (userAgent.contains("Edg")) return "Microsoft Edge";
        else if (userAgent.contains("Chrome")) return "Google Chrome";
        else return "Unknown";
    }

    private static void printAttributes(Object object) {
        if (object != null) {
            var context = object instanceof ServletContext ? (ServletContext) object : null;
            var session = object instanceof HttpSession ? (HttpSession) object : null;
            var request = object instanceof HttpServletRequest ? (HttpServletRequest) object : null;

            Enumeration<String> attributeNames = null;
            if (context != null) attributeNames = context.getAttributeNames();
            else if (session != null) attributeNames = session.getAttributeNames();
            else if (request != null) attributeNames = request.getAttributeNames();

            if (!attributeNames.hasMoreElements()) System.out.println("No attributes");
            while (attributeNames != null && attributeNames.hasMoreElements()) {
                String attributeName = attributeNames.nextElement();
                Object attributeValue = null;
                if (context != null) attributeValue = context.getAttribute(attributeName);
                else if (session != null) attributeValue = session.getAttribute(attributeName);
                else if (request != null) attributeValue = request.getAttribute(attributeName);
                System.out.println(attributeName + " = " + attributeValue);
            }
        }
    }
}
