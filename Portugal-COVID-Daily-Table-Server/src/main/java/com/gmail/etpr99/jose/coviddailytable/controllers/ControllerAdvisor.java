package com.gmail.etpr99.jose.coviddailytable.controllers;

import com.gmail.etpr99.jose.coviddailytable.exceptions.ReportInDateNonExistentException;
import com.gmail.etpr99.jose.coviddailytable.exceptions.TodayReportNonExistentException;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@ControllerAdvice
public class ControllerAdvisor extends ResponseEntityExceptionHandler {
    private final HttpHeaders httpHeaders;

    public ControllerAdvisor() {
        httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(org.springframework.http.MediaType.TEXT_PLAIN);
    }

    @ExceptionHandler(TodayReportNonExistentException.class)
    public ResponseEntity<Object> handleTodayReportNonFoundException() {
        return new ResponseEntity<>("Today's report data is still not available!", httpHeaders, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ReportInDateNonExistentException.class)
    public ResponseEntity<Object> handleReportInDateNonExistentException(WebRequest request) {
        return new ResponseEntity<>("There doesn't seem to be report data in the date of "
                + request.getParameter("date") + "!", httpHeaders, HttpStatus.NOT_FOUND);
    }
}
