package org.amirk.website.controllers;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/*
 * Just a global error handler for all controller methods annotated with @RequestMapping.
 * Sets status to 500 and prints exception details to the page.
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(Throwable.class)
    public String globalExceptionHandler(Exception exception, HttpServletRequest req, HttpServletResponse resp){
        
        String msg = "Unknown error";
        String stack = "Unknown stack";
        
        if(exception != null){
            if(exception.getMessage() != null){ msg = exception.getMessage(); }
            
            StackTraceElement[] traceElems = exception.getStackTrace();
            if(!ArrayUtils.isEmpty(traceElems)){
                StringBuilder bldr = new StringBuilder();
                for(StackTraceElement next : traceElems){
                    bldr.append(next.toString());
                    bldr.append("<br/>");
                }
                stack = bldr.toString();
            }
        }
        
        req.setAttribute("message", msg);
        req.setAttribute("stack", stack);
        resp.setStatus(500);
        
        return "errors/globalError";
    }
}
