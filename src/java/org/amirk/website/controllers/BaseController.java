package org.amirk.website.controllers;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/*
 * For now, this base controller is just a home for some shared functionality.
 */
public abstract class BaseController {
    
    /*
     * Flash messages have a type - defined by this enum.
     * The types are simply semantic - use them to color UI elements
     * according to the type (ie: 'error' might be red, 'success' green, etc.)
     */
    public enum FlashType {
        
        ERROR("error"),
        INFO("info"),
        SUCCESS("success");
        
        private String key;
        FlashType(String newkey){ this.key = newkey; }
        String getKey(){ return this.key; }
    }
    
    /*
     * Helper that returns a redirect string for the given url,
     * or throws an exception if the given url is null or empty.
     */
    protected String redirect(String url){
        if(StringUtils.isBlank(url)){ throw new IllegalArgumentException("Cannot redirect to null or empty url"); }
        
        return (url.charAt(0) == '/') ? 
                    "redirect:" + url : 
                    "redirect:/" + url;
    }
    
    /*
     * The flash helpers are convenience methods for populating a message
     * in the flash, and then immediately returning a redirect string for
     * the given url.
     */
    
    protected String flashErrorAndRedirect(String url, String message, RedirectAttributes flash){
        return flashFeedbackAndRedirect(FlashType.ERROR, url, message, flash);
    }
    
    protected String flashInfoAndRedirect(String url, String message, RedirectAttributes flash){
        return flashFeedbackAndRedirect(FlashType.INFO, url, message, flash);
    }
    
    protected String flashSuccessAndRedirect(String url, String message, RedirectAttributes flash){
        return flashFeedbackAndRedirect(FlashType.SUCCESS, url, message, flash);
    }
    
    protected String flashFeedbackAndRedirect(FlashType type, String url, String message, RedirectAttributes flash){
        flash.addFlashAttribute(type.getKey(), message);
        return redirect(url);
    }
}
