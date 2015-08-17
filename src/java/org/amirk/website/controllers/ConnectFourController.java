package org.amirk.website.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping(value="/connectfour")
public class ConnectFourController {
    
    @RequestMapping(method = RequestMethod.GET)
    public String index(){ return "connectfour/index"; }
}
