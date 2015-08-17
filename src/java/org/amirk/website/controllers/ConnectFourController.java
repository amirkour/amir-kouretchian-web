package org.amirk.website.controllers;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(value="/connectfour")
public class ConnectFourController extends BaseController {
    
    @RequestMapping(method = RequestMethod.GET)
    public String index(){ return "connectfour/index"; }
    
    @RequestMapping(method=RequestMethod.POST, value="/play")
    public String playNewGame(RedirectAttributes flash){
        throw new IllegalArgumentException("fooberloob");
    }
}
